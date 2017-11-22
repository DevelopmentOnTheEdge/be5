package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OperationServiceImpl implements OperationService
{
    public static final Logger log = Logger.getLogger(OperationServiceImpl.class.getName());

    private final OperationExecutor operationExecutor;
    private final Validator validator;

    public OperationServiceImpl(OperationExecutor operationExecutor, Validator validator)
    {
        this.operationExecutor = operationExecutor;
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
            return execute(operation, presetValues);
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
        Object parameters = null;
        try{
            parameters = operationExecutor.execute(operation, presetValues);
        }catch (Throwable ignore){ }

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

            OperationResult invokeResult = operation.getResult();
            operation.setResult(OperationResult.open());
            Object newParameters = operationExecutor.generate(operation, presetValues);

            if(OperationStatus.ERROR == operation.getStatus())
            {
                return Either.second(invokeResult);
            }

            if (newParameters instanceof DynamicPropertySet)
            {
                try
                {
                    validator.isError((DynamicPropertySet) newParameters);
                }
                catch (RuntimeException e)
                {
                    log.log(Level.FINE, "error in isError", e);
                    operation.setResult(OperationResult.error(e));
                    return replaceNullValueToEmptyStringAndReturn(operation, newParameters);
                }
            }

            if(newParameters != null)
            {
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
