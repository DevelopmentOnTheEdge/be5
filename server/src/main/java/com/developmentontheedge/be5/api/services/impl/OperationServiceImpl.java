package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.Either;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.components.FrontendConstants.RELOAD_CONTROL_NAME;


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
        operation.setResult(OperationResult.generate());

        Object parameters = operationExecutor.generate(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        try
        {
            validator.isError(parameters);
        }
        catch (RuntimeException e)
        {
            log.log(Level.INFO, "error on generate in parameters", e);
            operation.setResult(OperationResult.error(e));
            return replaceNullValueToEmptyStringAndReturn(parameters);
        }

        if (parameters == null)
        {
            return execute(operation, presetValues);
        }

        if(presetValues.containsKey(RELOAD_CONTROL_NAME))
        {
            try
            {
                validator.checkErrorAndCast(parameters);
            }
            catch (RuntimeException e)
            {
                log.log(Level.INFO, "error on generate in validate parameters", e);
                operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(parameters);
            }
        }

        return replaceNullValueToEmptyStringAndReturn(parameters);
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation, Map<String, Object> presetValues)
    {
        operation.setResult(OperationResult.execute());

        Object parameters = operationExecutor.execute(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            try
            {
                validator.isError(parameters);
            }
            catch (RuntimeException e)
            {
                log.log(Level.INFO, "error on execute in parameters", e);
                operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(parameters);
            }

            OperationResult invokeResult = operation.getResult();

            operation.setResult(OperationResult.generate());

            Object newParameters = operationExecutor.generate(operation, presetValues);

            if(OperationStatus.ERROR == operation.getStatus())
            {
                return Either.second(invokeResult);
            }

            if(newParameters != null)
            {
                operation.setResult(invokeResult);
                return replaceNullValueToEmptyStringAndReturn(newParameters);
            }
            else
            {
                throw (RuntimeException) invokeResult.getDetails();
            }
        }

        return Either.second(operation.getResult());
    }

    private Either<Object, OperationResult> replaceNullValueToEmptyStringAndReturn(Object parameters)
    {
        validator.replaceNullValueToEmptyString(parameters);

        return Either.first(parameters);
    }

}
