package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.inject.Injector;
import com.developmentontheedge.be5.metadata.model.GroovyOperationExtender;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationExtender;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.TransactionalOperation;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.metadata.model.Operation.*;


public class OperationExecutorImpl implements OperationExecutor
{
    public static final Logger log = Logger.getLogger(OperationExecutorImpl.class.getName());

    private final Injector injector;
    private final DatabaseService databaseService;
    private final Validator validator;
    private final GroovyOperationLoader groovyOperationLoader;
    private final UserAwareMeta userAwareMeta;
    private final GroovyRegister groovyRegister;

    public OperationExecutorImpl(Injector injector, DatabaseService databaseService, Validator validator,
                                 GroovyOperationLoader groovyOperationLoader, UserAwareMeta userAwareMeta,
                                 GroovyRegister groovyRegister)
    {
        this.injector = injector;
        this.databaseService = databaseService;
        this.validator = validator;
        this.groovyOperationLoader = groovyOperationLoader;
        this.userAwareMeta = userAwareMeta;
        this.groovyRegister = groovyRegister;
    }

    @Override
    public Object generate(Operation operation, Map<String, Object> presetValues)
    {
        try
        {
            return operation.getParameters(presetValues);
        }
        catch (Throwable e)
        {
            Be5Exception be5Exception = Be5Exception.internalInOperation(e, operation.getInfo().getModel());
            //operation.setResult(OperationResult.error(be5Exception));
            throw be5Exception;
        }
    }

    @Override
    public Object execute(Operation operation, Map<String, Object> presetValues)
    {
        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transactionWithResult(connection -> {
                Object parameters = callOperation(operation, presetValues);
                if(operation.getStatus() == OperationStatus.ERROR)
                {
                    databaseService.rollback(connection, (Throwable) operation.getResult().getDetails());
                }
                return parameters;
            });
        }
        else
        {
            return callOperation(operation, presetValues);
        }
    }

    private Object callOperation(Operation operation, Map<String, Object> presetValues)
    {
        Object parameters = generate(operation, presetValues);

        if(operation.getStatus() == OperationStatus.ERROR)
        {
            return parameters;
        }

        try
        {
            validator.checkErrorAndCast(parameters);
        }
        catch (RuntimeException e)
        {
            log.log(Level.INFO, "error on execute in validate parameters", e);
            operation.setResult(OperationResult.error(e));
            return parameters;
        }

        return callInvoke(operation, parameters);
    }

    private Object callInvoke(Operation operation, Object parameters)
    {
        try
        {
            doInvokeOperation(operation, parameters);

            if(operation.getStatus() == OperationStatus.ERROR)
            {
                return parameters;
            }

            try
            {
                validator.isError(parameters);
            }
            catch (RuntimeException e)
            {
                log.log(Level.INFO, "error on execute in parameters", e);
                operation.setResult(OperationResult.error(e));
                return parameters;
            }

            return null;
        }
        catch (Throwable e)
        {
            Be5Exception be5Exception = Be5Exception.internalInOperation(e, operation.getInfo().getModel());
            operation.setResult(OperationResult.error(be5Exception));
            return parameters;
        }
    }

    private void doInvokeOperation(Operation op, Object parameters) throws Exception
    {
        List<OperationExtender> operationExtenders = loadOperationExtenders(op);
        invokeExtenders( "preInvoke", op, operationExtenders, parameters);
        if( !invokeExtenders( "skipInvoke", op, operationExtenders, parameters) )
        {
            op.invoke(parameters);
            invokeExtenders( "postInvoke", op, operationExtenders, parameters);
        }
        else
        {
            if(OperationStatus.EXECUTE == op.getStatus())
            {
                op.setResult(OperationResult.finished("Invokation of operation is cancelled by extender"));
            }
        }
    }

    private List<OperationExtender> loadOperationExtenders(Operation operation)
    {
        if(operation.getInfo().getModel().getExtenders() == null) return Collections.emptyList();

        List<com.developmentontheedge.be5.metadata.model.OperationExtender> operationExtenderModels =
                operation.getInfo().getModel().getExtenders().getAvailableElements()
                        .stream()
                        .sorted(Comparator.comparing(com.developmentontheedge.be5.metadata.model.
                                OperationExtender::getInvokeOrder))
                        .collect(Collectors.toList());

        List<OperationExtender> operationExtenders = new ArrayList<>();

        for (com.developmentontheedge.be5.metadata.model.OperationExtender
                operationExtenderModel : operationExtenderModels)
        {
            OperationExtender operationExtender;

            if(operationExtenderModel.getClass() == GroovyOperationExtender.class)
            {
                GroovyOperationExtender groovyExtender = (GroovyOperationExtender)operationExtenderModel;
                try
                {
                    Class aClass = groovyRegister.getClass("groovyExtender-" + groovyExtender.getFileName(),
                            groovyExtender.getCode(), groovyExtender.getFileName());
                    if(aClass != null)
                    {
                        operationExtender = ( OperationExtender ) aClass.newInstance();
                    }
                    else
                    {
                        throw Be5Exception.internalInOperationExtender(
                                new Error("Class " + groovyExtender.getCode() + " is null."), groovyExtender);
                    }
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                catch ( Throwable e )
                {
                    throw Be5Exception.internalInOperationExtender(e, groovyExtender);
                }
            }
            else
            {
                try
                {
                    operationExtender =
                            (OperationExtender) Class.forName(operationExtenderModel.getClassName()).newInstance();
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInOperationExtender(e, operationExtenderModel);
                }
            }

            injector.injectAnnotatedFields(operationExtender);

            operationExtenders.add(operationExtender);
        }

        return operationExtenders;
    }

    private boolean invokeExtenders(String action, Operation curOp, List<OperationExtender> operationExtenders, Object parameters) throws Exception
    {
        for( OperationExtender ext : operationExtenders )
        {
            switch (action)
            {
                case "skipInvoke":
                    return ext.skipInvoke(curOp, parameters);
                case "preInvoke":
                    ext.preInvoke(curOp, parameters);
                    break;
                case "postInvoke":
                    ext.postInvoke(curOp, parameters);
                    break;
            }
        }
        return false;
    }
//
//    @Override
//    public Operation create(OperationInfo operationInfo, String queryName,
//                            String[] selectedRows, Map<String, String> operationParams)
//    {
//        OperationContext operationContext = new OperationContext(selectedRows, queryName, operationParams);
//
//        return create(operationInfo, operationContext);
//    }

    @Override
    public Operation create(String entityName, String queryName, String operationName,
                            String[] selectedRows, Map<String, Object> operationParams)
    {
        OperationInfo operationInfo = userAwareMeta.getOperation(entityName, queryName, operationName);

        OperationContext operationContext = new OperationContext(selectedRows, queryName, operationParams);

        return create(operationInfo, operationContext);
    }

    @Override
    public Operation create(OperationInfo operationInfo, OperationContext operationContext)
    {
        Operation operation;

        switch (operationInfo.getType())
        {
            case OPERATION_TYPE_GROOVY:
                try
                {
                    Class aClass = groovyOperationLoader.get(operationInfo);
                    if(aClass != null)
                    {
                        operation = ( Operation ) aClass.newInstance();
                    }
                    else
                    {
                        throw Be5Exception.internalInOperation(
                                new Error("Class " + operationInfo.getCode() + " is null."), operationInfo.getModel());
                    }
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                catch ( Throwable e )
                {
                    throw Be5Exception.internalInOperation(e, operationInfo.getModel());
                }
                break;
            case OPERATION_TYPE_JAVA:
                try
                {
                    operation = ( Operation ) Class.forName(operationInfo.getCode()).newInstance();
                    break;
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInOperation(e, operationInfo.getModel());
                }
            case OPERATION_TYPE_JAVAFUNCTION:
            case OPERATION_TYPE_SQL:
            case OPERATION_TYPE_JAVASCRIPT:
            case OPERATION_TYPE_JSSERVER:
            case OPERATION_TYPE_DOTNET:
            case OPERATION_TYPE_JAVADOTNET:
                throw Be5Exception.internal("Not support operation type: " + operationInfo.getType());
            default:
                throw Be5Exception.internal("Unknown action type '" + operationInfo.getType() + "'");
        }

        operation.initialize(operationInfo, operationContext, OperationResult.create());
        injector.injectAnnotatedFields(operation);

        return operation;
    }

}
