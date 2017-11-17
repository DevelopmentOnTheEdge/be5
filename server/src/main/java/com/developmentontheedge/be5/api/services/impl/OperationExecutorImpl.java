package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;

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
    public void execute(Operation operation, Map<String, Object> presetValues)
    {
        OperationContext operationContext = new OperationContext(operation.getRecords(), operation.getInfo().getQueryName());

        if(operation instanceof TransactionalOperation)
        {
            databaseService.transaction(connection -> {
                callOperation(presetValues, operation, operationContext);
                return null;
            });
        }
        else
        {
            callOperation(presetValues, operation, operationContext);
        }
    }

    private void callOperation(Map<String, Object> presetValues, Operation operation,
             OperationContext operationContext)
    {
        Object parameters = generate(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return;// operation.getResult();
        }

        if (parameters instanceof DynamicPropertySet)
        {
            if(operation instanceof GOperationSupport)
            {
                ((GOperationSupport) operation).dps = (GDynamicPropertySetSupport) parameters;
            }

            try
            {
                validator.checkErrorAndCast((DynamicPropertySet) parameters);
            }
            catch (RuntimeException e)
            {
                Be5Exception be5Exception = Be5Exception.internalInOperation(e, operation.getInfo().getModel());
                operation.setResult(OperationResult.error(be5Exception));
                return;// operation.getResult();
                //throw Be5Exception.internalInOperationParameter(e, operation.getInfo().getModel());
            }
        }

        callInvoke(operation, parameters, operationContext);
    }

    @Override
    public void callInvoke(Operation operation, Object parameters, OperationContext operationContext)
    {
        operation.setResult(OperationResult.progress());

        try
        {
            operation.invoke(parameters, operationContext);

            if(OperationStatus.ERROR == operation.getStatus())
            {
                return;// operation.getResult();
            }

            if (parameters instanceof DynamicPropertySet)
            {
                try
                {
                    validator.isError((DynamicPropertySet) parameters);
                }
                catch (RuntimeException e)
                {
                    Be5Exception be5Exception = Be5Exception.internalInOperation(e, operation.getInfo().getModel());
                    operation.setResult(OperationResult.error(be5Exception));
                    return;
                    //throw Be5Exception.internalInOperation(e.getCause(), operation.getInfo().getModel());
                }
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
        }
        catch (Be5Exception e)
        {
            throw e;//Be5Exception.internalInOperation(e.getCause(), operation.getInfo())
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo().getModel());
        }
//        catch (Throwable e)
//        {
//            Be5Exception be5Exception = Be5Exception.internalInOperation(e, operation.getInfo().getModel());
//            operation.setResult(OperationResult.error(be5Exception));
//            //throw Be5Exception.internalInOperation(e.getCause(), operation.getInfo().getModel());
//        }
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
