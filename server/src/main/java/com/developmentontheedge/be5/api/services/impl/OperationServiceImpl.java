package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OperationServiceImpl implements OperationService
{
    public static final Logger log = Logger.getLogger(OperationServiceImpl.class.getName());

    private final OperationExecutor operationExecutor;
    private final DatabaseService databaseService;
    private final Validator validator;

    public OperationServiceImpl(OperationExecutor operationExecutor,
                                DatabaseService databaseService, Validator validator)
    {
        this.operationExecutor = operationExecutor;
        this.databaseService = databaseService;
        this.validator = validator;
    }

    @Override
    public Either<Object, OperationResult> generate(Operation operation, Map<String, Object> presetValues)
    {
        Object parameters = operationExecutor.generate(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        if (parameters instanceof DynamicPropertySet)
        {
            try
            {
                validator.isError((DynamicPropertySet) parameters);
            }
            catch (RuntimeException e)
            {
                log.log(Level.FINE, "error in isError", e);
                operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(operation, parameters);
            }
        }

        //run manually in component
        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(operation.getRecords(), operation.getInfo().getQueryName());
            if(operation instanceof TransactionalOperation)
            {
                return databaseService.transaction((connection) ->
                        callInvoke(operation, presetValues,
                                null, operationContext)
                );
            }
            else
            {
                return callInvoke(operation, presetValues,
                        null, operationContext);
            }
        }

        operation.setResult(OperationResult.open());
        if(parameters instanceof DynamicPropertySet)
        {
            if(presetValues.containsKey(OperationSupport.reloadControl))
            {
                try
                {
                    validator.checkErrorAndCast((DynamicPropertySet) parameters);
                }
                catch (RuntimeException e)
                {
                    log.log(Level.FINE, "error in validate", e);
                    operation.setResult(OperationResult.error(e));
                    return replaceNullValueToEmptyStringAndReturn(operation, parameters);
                }
            }
        }

        return replaceNullValueToEmptyStringAndReturn(operation, parameters);
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation, Map<String, Object> presetValues)
    {
        OperationContext operationContext = new OperationContext(operation.getRecords(), operation.getInfo().getQueryName());

        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transaction(connection ->
                    callOperation(operation, presetValues, operationContext)
            );
        }
        else
        {
            return callOperation(operation, presetValues, operationContext);
        }
    }

    private Either<Object, OperationResult> callOperation( Operation operation, Map<String, Object> presetValues,
                                                                                    OperationContext operationContext)
    {
        Object parameters = operationExecutor.generate(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        if(parameters instanceof DynamicPropertySet)
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
                log.log(Level.FINE, "error in validate", e);
                operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(operation, parameters);
            }
        }

        return callInvoke(operation, presetValues, parameters, operationContext);
    }

    private Either<Object, OperationResult> callInvoke(Operation operation, Map<String, Object> presetValues,
            Object parameters, OperationContext operationContext)
    {
        operationExecutor.callInvoke(operation, parameters, operationContext);

        if(operation.getStatus() == OperationStatus.ERROR)
        {
            if (parameters instanceof DynamicPropertySet)
            {
                try
                {
                    validator.isError((DynamicPropertySet) parameters);
                }
                catch (RuntimeException e)
                {
                    log.log(Level.FINE, "error in isError", e);
                    operation.setResult(OperationResult.error(e));
                    return replaceNullValueToEmptyStringAndReturn(operation, parameters);
                }
            }

            if(OperationStatus.ERROR == operation.getStatus() && parameters != null)
            {
                OperationResult invokeResult = operation.getResult();
                Object newParameters = operationExecutor.generate(operation, presetValues);
                operation.setResult(invokeResult);
                return replaceNullValueToEmptyStringAndReturn(operation, newParameters);
            }
        }

        return Either.second(operation.getResult());
    }

    private Either<Object, OperationResult> replaceNullValueToEmptyStringAndReturn(Operation operation, Object parameters)
    {
        validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

        return Either.first(parameters);
    }

}
