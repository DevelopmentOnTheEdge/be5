package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
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
import com.developmentontheedge.beans.DynamicPropertySet;

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
    public Object generate(OperationInfo meta, Map<String, Object> presetValues, String[] selectedRows, Request req)
    {
        Operation operation = create(meta, selectedRows, req);

        return callGetParameters(operation, presetValues);
    }

    private Object callGetParameters(Operation operation, Map<String, Object> presetValues)
    {
        Object parameters;

        try
        {
            parameters = operation.getParameters(presetValues);
        }
        catch (Throwable e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo().getModel());
        }

        if(OperationStatus.ERROR == operation.getStatus())
        {
            throw Be5Exception.internalInOperation(new Exception("ERROR Status"), operation.getInfo().getModel());
        }

        if(parameters instanceof DynamicPropertySet)
        {
            validator.isError((DynamicPropertySet) parameters);
            validator.checkErrorAndCast((DynamicPropertySet) parameters);
        }

        return parameters;
    }

    @Override
    public void execute(OperationInfo meta, Map<String, Object> presetValues, String[] selectedRows, Request req)
    {
        Operation operation = create(meta, selectedRows, req);
        OperationContext operationContext = new OperationContext(selectedRows, meta.getQueryName());

        if(operation instanceof TransactionalOperation)
        {
            databaseService.transaction(connection ->
                    callOperation(presetValues, operation, operationContext)
            );
        }
        else
        {
            callOperation(presetValues, operation, operationContext);
        }
    }

    private OperationResult callOperation(Map<String, Object> presetValues, Operation operation,
             OperationContext operationContext)
    {
        Object parameters = callGetParameters(operation, presetValues);

        return callInvoke(operation, parameters, operationContext);
    }

    private OperationResult callInvoke(Operation operation,
         Object parameters, OperationContext operationContext)
    {
        operation.setResult(OperationResult.progress());

        try
        {
            operation.invoke(parameters, operationContext);
        }
        catch (Throwable e)
        {
            throw Be5Exception.internalInOperation(e.getCause(), operation.getInfo().getModel());
        }

        if(OperationStatus.ERROR == operation.getStatus())
        {
            throw Be5Exception.internalInOperation(new Exception("ERROR Status"), operation.getInfo().getModel());
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

        return operation.getResult();
    }

    @Override
    public Operation create(OperationInfo operationInfo, String[] records, Request request)
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
                        //throw Be5Exception.internal("Class " + operationInfo.getCode() + " is null." );
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

        operation.initialize(operationInfo, OperationResult.open(), records, request);
        injector.injectAnnotatedFields(operation);

        return operation;
    }

}
