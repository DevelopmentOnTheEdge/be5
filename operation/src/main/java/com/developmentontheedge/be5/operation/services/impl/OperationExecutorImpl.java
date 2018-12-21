package com.developmentontheedge.be5.operation.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.util.Utils;
import com.developmentontheedge.be5.database.ConnectionService;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.GroovyOperationExtender;
import com.developmentontheedge.be5.metadata.model.JavaOperation;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationContext;
import com.developmentontheedge.be5.operation.model.OperationExtender;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import com.developmentontheedge.be5.operation.model.TransactionalOperation;
import com.developmentontheedge.be5.operation.services.GroovyOperationLoader;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.validation.Validator;
import com.developmentontheedge.be5.operation.util.OperationUtils;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class OperationExecutorImpl implements OperationExecutor
{
    public static final Logger log = Logger.getLogger(OperationExecutorImpl.class.getName());

    private final Injector injector;
    private final ConnectionService connectionService;
    private final Validator validator;
    private final GroovyOperationLoader groovyOperationLoader;
    private final Meta meta;
    private final GroovyRegister groovyRegister;

    @Inject
    public OperationExecutorImpl(Injector injector, ConnectionService connectionService, Validator validator,
                                 GroovyOperationLoader groovyOperationLoader, Meta meta, GroovyRegister groovyRegister)
    {
        this.injector = injector;
        this.connectionService = connectionService;
        this.validator = validator;
        this.groovyOperationLoader = groovyOperationLoader;
        this.meta = meta;
        this.groovyRegister = groovyRegister;
    }

    @Override
    public Object generate(Operation operation, Map<String, Object> presetValues)
    {
        List<OperationExtender> extenders = loadOperationExtenders(operation);
        return generateWithExtenders(operation, extenders, presetValues);
    }

    private Object generateWithExtenders(Operation operation, List<OperationExtender> extenders,
                                         Map<String, Object> presetValues)
    {
        try
        {
            Object parameters = operation.getParameters(presetValues);
            for (OperationExtender ext : extenders)
            {
                parameters = ext.postGetParameters(operation, parameters, presetValues);
            }
            return parameters;
        }
        catch (Throwable e)
        {
            throw Be5Exception.internalInOperation(operation.getInfo().getModel(), e);
        }
    }

    @Override
    public Object execute(Operation operation, Map<String, Object> presetValues)
    {
        List<OperationExtender> extenders = loadOperationExtenders(operation);
        if (operation instanceof TransactionalOperation)
        {
            return connectionService.transactionWithResult(connection -> {
                Object parameters = callOperation(operation, extenders, presetValues);
                if (operation.getStatus() == OperationStatus.ERROR)
                {
                    connectionService.rollbackTransaction();
                }
                return parameters;
            });
        }
        else
        {
            return callOperation(operation, extenders, presetValues);
        }
    }

    private Object callOperation(Operation operation, List<OperationExtender> extenders,
                                 Map<String, Object> presetValues)
    {
        Object parameters = generateWithExtenders(operation, extenders, presetValues);

        if (operation.getStatus() == OperationStatus.ERROR)
        {
            return parameters;
        }

        try
        {
            validator.checkAndThrowExceptionIsError(parameters);
        }
        catch (RuntimeException e)
        {
            log.log(Level.INFO, "error on execute in validate parameters", e);
            operation.setResult(OperationResult.error(e.getMessage(), e));
            return parameters;
        }

        return callInvoke(operation, extenders, parameters);
    }

    private Object callInvoke(Operation operation, List<OperationExtender> extenders, Object parameters)
    {
        try
        {
            doInvokeWithExtenders(operation, extenders, parameters);

            if (operation.getStatus() == OperationStatus.ERROR)
            {
                return parameters;
            }

            try
            {
                validator.throwExceptionIsError(parameters);
            }
            catch (RuntimeException e)
            {
                //to do: change message - error state in parameter
                log.log(Level.INFO, "error on execute in parameters", e);
                operation.setResult(OperationResult.error(e.getMessage(), e));
                return parameters;
            }

            return null;
        }
        catch (Throwable e)
        {
            Be5Exception be5Exception = Be5Exception.internalInOperation(operation.getInfo().getModel(), e);
            operation.setResult(OperationResult.error(be5Exception));

            log.log(Level.SEVERE, "error on callInvoke", be5Exception);
            return parameters;
        }
    }

    private void doInvokeWithExtenders(Operation op, List<OperationExtender> extenders, Object parameters)
            throws Exception
    {
        invokeExtenders("preInvoke", op, extenders, parameters);
        if (op.getStatus() == OperationStatus.ERROR) return;
        if (!invokeExtenders("skipInvoke", op, extenders, parameters))
        {
            if (op.getStatus() == OperationStatus.ERROR) return;
            op.invoke(parameters);
            if (op.getStatus() == OperationStatus.ERROR) return;
            invokeExtenders("postInvoke", op, extenders, parameters);
        }
        else
        {
            if (OperationStatus.EXECUTE == op.getStatus())
            {
                op.setResult(OperationResult.finished("Invocation of operation is cancelled by extender"));
            }
        }
    }

    private List<OperationExtender> loadOperationExtenders(Operation operation)
    {
        if (operation.getInfo().getModel().getExtenders() == null) return Collections.emptyList();

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

            if (operationExtenderModel.getClass() == GroovyOperationExtender.class)
            {
                GroovyOperationExtender groovyExtender = (GroovyOperationExtender) operationExtenderModel;
                try
                {
                    Class aClass = groovyRegister.getClass("groovyExtender-" + groovyExtender.getFileName(),
                            groovyExtender.getCode(), groovyExtender.getFileName());
                    if (aClass != null)
                    {
                        operationExtender = (OperationExtender) aClass.newInstance();
                    }
                    else
                    {
                        throw Be5Exception.internalInOperationExtender(groovyExtender,
                                new RuntimeException("Class " + groovyExtender.getCode() + " is null."));
                    }
                }
                catch (NoClassDefFoundError | IllegalAccessException | InstantiationException e)
                {
                    throw new UnsupportedOperationException("Groovy feature has been excluded", e);
                }
                catch (Throwable e)
                {
                    throw Be5Exception.internalInOperationExtender(groovyExtender, e);
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
                    throw Be5Exception.internalInOperationExtender(operationExtenderModel, e);
                }
            }

            injector.injectMembers(operationExtender);

            operationExtenders.add(operationExtender);
        }

        return operationExtenders;
    }

    private boolean invokeExtenders(String action, Operation curOp, List<OperationExtender> operationExtenders,
                                    Object parameters) throws Exception
    {
        for (OperationExtender ext : operationExtenders)
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
                default:
                    throw Be5Exception.internal("Not support invoke action.");
            }
            if (curOp.getStatus() == OperationStatus.ERROR) return false;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OperationContext getOperationContext(OperationInfo operationInfo, String queryName,
                                                Map<String, ?> operationParams)
    {
        Object[] selectedRows = OperationUtils.
                selectedRows((String) operationParams.get(OperationConstants.SELECTED_ROWS));
        if (selectedRows.length > 0)
        {
            if (!operationInfo.getEntityName().startsWith("_"))
            {
                if (!operationInfo.getEntity().hasPrimaryKey())
                {
                    throw Be5Exception.internalInOperation(operationInfo.getModel(),
                            new RuntimeException("Entity '" + operationInfo.getEntity().getName() +
                                    "' does not have primary key."));
                }
                Class<?> primaryKeyColumnType = meta.
                        getColumnType(operationInfo.getEntity(), operationInfo.getPrimaryKey());
                selectedRows = Utils.changeTypes(selectedRows, primaryKeyColumnType);
            }
        }
        return new OperationContext(selectedRows, queryName, (Map<String, Object>) operationParams);
    }

    @Override
    public Operation create(OperationInfo operationInfo, String queryName,
                            Map<String, Object> operationParams)
    {
        OperationContext operationContext = getOperationContext(operationInfo, queryName, operationParams);

        return create(operationInfo, operationContext);
    }

    @Override
    public Operation create(OperationInfo operationInfo, OperationContext operationContext)
    {
        Operation operation;

        if (operationInfo.getModel().getClass() == GroovyOperation.class)
        {
            try
            {
                Class aClass = groovyOperationLoader.get((GroovyOperation) operationInfo.getModel());
                if (aClass != null)
                {
                    operation = (Operation) aClass.newInstance();
                }
                else
                {
                    throw Be5Exception.internalInOperation(operationInfo.getModel(),
                            new Error("Class " + operationInfo.getCode() + " is null."));
                }
            }
            catch (NoClassDefFoundError | IllegalAccessException | InstantiationException e)
            {
                throw new UnsupportedOperationException("Groovy feature has been excluded", e);
            }
            catch (Throwable e)
            {
                throw Be5Exception.internalInOperation(operationInfo.getModel(), e);
            }
        }
        else if (operationInfo.getModel().getClass() == JavaOperation.class)
        {
            try
            {
                operation = (Operation) Class.forName(operationInfo.getCode()).newInstance();
            }
            catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
            {
                throw Be5Exception.internalInOperation(operationInfo.getModel(), e);
            }
        }
        else
        {
            throw Be5Exception.internal("Not support operation type: " + operationInfo.getType());
        }

        injector.injectMembers(operation);
        operation.initialize(operationInfo, operationContext, OperationResult.create());

        return operation;
    }

}
