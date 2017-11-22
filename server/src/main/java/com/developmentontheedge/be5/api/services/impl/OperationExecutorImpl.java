package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Map;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;


public class OperationExecutorImpl implements OperationExecutor
{
    private final Injector injector;
    private final DatabaseService databaseService;
    private final Validator validator;
    private final GroovyOperationLoader groovyOperationLoader;

    public OperationExecutorImpl(GroovyOperationLoader groovyOperationLoader, Injector injector,
                                 DatabaseService databaseService, Validator validator)
    {
        this.injector = injector;
        this.databaseService = databaseService;
        this.validator = validator;
        this.groovyOperationLoader = groovyOperationLoader;
    }

    @Override
    public Object generate(Operation operation, Map<String, Object> presetValues)
    {
        try
        {
            return operation.getParameters(presetValues);
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo().getModel());
        }
    }

    @Override
    public Object execute(Operation operation, Map<String, Object> presetValues)
    {
        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transaction(connection ->
                callOperation(operation, presetValues)
            );
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
            operation.setResult(OperationResult.error(e));
            return parameters;
        }

        return callInvoke(operation, parameters);
    }

    private Object callInvoke(Operation operation, Object parameters)
    {
        operation.setResult(OperationResult.progress());

        try
        {
            OperationContext operationContext = new OperationContext(operation.getRecords(), operation.getInfo().getQueryName());
            operation.invoke(parameters, operationContext);

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
                operation.setResult(OperationResult.error(e));
                return parameters;
            }

            if(OperationStatus.IN_PROGRESS == operation.getStatus())
            {
                operation.setResult(OperationResult.redirect(
                        new HashUrl(FrontendConstants.TABLE_ACTION,
                                operation.getInfo().getEntityName(),
                                operation.getInfo().getQueryName())
                                .named(operation.getRedirectParams())
                ));
            }

            return null;
        }
        catch (Throwable e)
        {
            operation.setResult(OperationResult.error(e));
            return parameters;
        }
    }

    @Override
    public Operation create(OperationInfo operationInfo, String[] records)
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
            default:
                try
                {
                    operation = ( Operation ) Class.forName(operationInfo.getCode()).newInstance();
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInOperation(new RuntimeException(
                            "It is possible to use the 'file:' instead of the 'code:' " +
                                    "in the yaml file. \n\t" + e.getMessage(), e), operationInfo.getModel());
                }
        }

        operation.initialize(operationInfo, OperationResult.open(), records);
        operation.setResult(OperationResult.progress());
        injector.injectAnnotatedFields(operation);

        return operation;
    }

}
