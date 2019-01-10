package com.developmentontheedge.be5.operation.services.impl;

import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.validation.Validator;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.util.FilterUtil;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.operation.OperationConstants.RELOAD_CONTROL_NAME;
import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;


public class OperationServiceImpl implements OperationService
{
    public static final Logger log = Logger.getLogger(OperationServiceImpl.class.getName());

    private final OperationExecutor operationExecutor;
    private final Validator validator;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public OperationServiceImpl(OperationExecutor operationExecutor, Validator validator, UserInfoProvider userInfoProvider)
    {
        this.operationExecutor = operationExecutor;
        this.validator = validator;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public Either<Object, OperationResult> generate(Operation operation, Map<String, Object> values)
    {
        Map<String, Object> presetValues = getPresetValues(operation.getContext(), values);

        operation.setResult(OperationResult.generate());

        Object parameters = operationExecutor.generate(operation, presetValues);

        if (OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        if (parameters == null)
        {
            return execute(operation, presetValues);
        }

        if (presetValues.containsKey(RELOAD_CONTROL_NAME))
        {
            if (parameters instanceof DynamicPropertySet)
            {
                String reloadControlName = ((String) presetValues.get(RELOAD_CONTROL_NAME)).substring(1);
                DynamicProperty property = ((DynamicPropertySet) parameters).getProperty(reloadControlName);
                validator.validate(property);
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

        if (OperationStatus.EXECUTE == operation.getStatus())
        {
            operation.setResult(OperationResult.redirect(new HashUrl(TABLE_ACTION,
                    operation.getInfo().getEntityName(), operation.getContext().getQueryName())
                    .named(FilterUtil.getOperationParamsWithoutFilter(operation.getRedirectParams())).toString())
            );
        }

        if (OperationStatus.ERROR == operation.getStatus())
        {
            try
            {
                validator.throwExceptionIsError(parameters);
            }
            catch (RuntimeException e)
            {
                log.log(Level.INFO, "error on execute in parameters", e);
                //remove duplicate operation.setResult(OperationResult.error(e));
                return replaceNullValueToEmptyStringAndReturn(parameters);
            }

            Operation newOperation = operationExecutor.create(operation.getInfo(), operation.getContext());
            Object newParameters = operationExecutor.generate(newOperation, presetValues);

            if (newParameters != null && OperationStatus.ERROR != newOperation.getStatus())
            {
                return replaceNullValueToEmptyStringAndReturn(newParameters);
            }

            return Either.second(operation.getResult());
        }

        return Either.second(operation.getResult());
    }

    private static Map<String, Object> getPresetValues(OperationContext context, Map<String, Object> values)
    {
        Map<String, Object> presetValues =
                new HashMap<>(FilterUtil.getOperationParamsWithoutFilter(context.getParams()));

        presetValues.putAll(values);
        return presetValues;
    }

    private static Either<Object, OperationResult> replaceNullValueToEmptyStringAndReturn(Object parameters)
    {
        replaceValuesToString(parameters);

        return Either.first(parameters);
    }

    static void replaceValuesToString(Object parameters)
    {
        if (parameters instanceof DynamicPropertySet)
        {
            for (DynamicProperty property : (DynamicPropertySet) parameters)
            {
                if (property.getValue() == null)
                {
                    property.setValue("");
                }
                else if (property.getValue().getClass() != String.class &&
                        property.getValue().getClass() != Boolean.class &&
                        !(property.getValue() instanceof Object[]))
                {
                    property.setValue(property.getValue().toString());
                }
            }
        }
    }


}
