package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.ParseRequestUtils;

import java.util.HashMap;
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
    public Either<Object, OperationResult> generate(Operation operation, Map<String, Object> values)
    {
        Map<String, Object> presetValues = getPresetValues(operation.getContext(), values);

        operation.setResult(OperationResult.generate());

        Object parameters = operationExecutor.generate(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
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
                if(UserInfoHolder.isSystemDeveloper())
                {
                    log.log(Level.INFO, "error on generate in validate parameters", e);
                    operation.setResult(OperationResult.error(e));
                }
            }
        }

        return replaceNullValueToEmptyStringAndReturn(parameters);
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation, Map<String, Object> values)
    {
        Map<String, Object> presetValues = getPresetValues(operation.getContext(), values);

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

            operation.setResult(OperationResult.execute());

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
                return Either.second(invokeResult);
            }
        }

        return Either.second(operation.getResult());
    }

    private Map<String, Object> getPresetValues(OperationContext context, Map<String, Object> values)
    {
        Map<String, Object> presetValues =
                new HashMap<>(ParseRequestUtils.getOperationParamsWithoutFilter(context.getOperationParams()));

        presetValues.putAll(values);
        return presetValues;
    }

    private Either<Object, OperationResult> replaceNullValueToEmptyStringAndReturn(Object parameters)
    {
        validator.replaceNullValueToEmptyString(parameters);

        return Either.first(parameters);
    }

}
