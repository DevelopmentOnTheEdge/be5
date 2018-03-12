package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.TransactionalOperation;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.model.Operation.*;


public class OperationExecutorImpl implements OperationExecutor
{
    public static final Logger log = Logger.getLogger(OperationExecutorImpl.class.getName());

    private final Injector injector;
    private final DatabaseService databaseService;
    private final Validator validator;
    private final GroovyOperationLoader groovyOperationLoader;
    private final UserAwareMeta userAwareMeta;

    public OperationExecutorImpl(Injector injector, DatabaseService databaseService, Validator validator, GroovyOperationLoader groovyOperationLoader, UserAwareMeta userAwareMeta)
    {
        this.injector = injector;
        this.databaseService = databaseService;
        this.validator = validator;
        this.groovyOperationLoader = groovyOperationLoader;
        this.userAwareMeta = userAwareMeta;
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
            operation.invoke(parameters);

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

            if(OperationStatus.EXECUTE == operation.getStatus())
            {
                operation.setResult(OperationResult.redirectToTable(
                    operation.getInfo().getEntityName(),
                    operation.getContext().getQueryName(),
                    operation.getRedirectParams()
                ));
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

    @Override
    public Operation create(String entityName, String queryName, String operationName)
    {
        return create(entityName, queryName, operationName, new String[]{}, Collections.emptyMap());
    }

    @Override
    public Operation create(String entityName, String queryName, String operationName,
                            String[] selectedRows, Map<String, String> operationParams)
    {
        OperationInfo operationInfo = userAwareMeta.getOperation(entityName, operationName);

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
                    throw Be5Exception.internalInOperation(new RuntimeException(
                            "It is possible to use the 'file:' instead of the 'code:' " +
                                    "in the yaml file. \n\t" + e.getMessage(), e), operationInfo.getModel());
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
