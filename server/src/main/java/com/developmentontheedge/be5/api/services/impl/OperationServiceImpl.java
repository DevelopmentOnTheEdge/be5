package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
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

import java.util.Collections;
import java.util.Map;


public class OperationServiceImpl implements OperationService
{
    private final OperationExecutor operationExecutor;
    private final DatabaseService databaseService;
    private final UserAwareMeta userAwareMeta;
    private final Validator validator;

    public OperationServiceImpl(OperationExecutor operationExecutor,
                                DatabaseService databaseService, Validator validator, UserAwareMeta userAwareMeta)
    {
        this.operationExecutor = operationExecutor;
        this.databaseService = databaseService;
        this.validator = validator;
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public Either<Object, OperationResult> generate(Operation operation)
    {
        return generate(operation, Collections.emptyMap());
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
                        callInvoke(presetValues, operation,
                                null, operationContext)
                );
            }
            else
            {
                return callInvoke(presetValues, operation,
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
                    operation.setResult(OperationResult.error(e));
                    return replaceNullValueToEmptyStringAndReturn(operation, parameters);
                }
            }
        }

        return replaceNullValueToEmptyStringAndReturn(operation, parameters);
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation)
    {
        return execute(operation, Collections.emptyMap());
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation, Map<String, Object> presetValues)
    {
        OperationContext operationContext = new OperationContext(operation.getRecords(), operation.getInfo().getQueryName());

        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transaction(connection ->
                    callOperation(presetValues, operation, operationContext)
            );
        }
        else
        {
            return callOperation(presetValues, operation, operationContext);
        }
    }

    private Either<Object, OperationResult> callOperation(
            Map<String, Object> presetValues, Operation operation,
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
                operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(operation, parameters);
            }
        }

        return callInvoke(presetValues, operation, parameters, operationContext);
    }

    private Either<Object, OperationResult> callInvoke(
            Map<String, Object> presetValues, Operation operation,
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
