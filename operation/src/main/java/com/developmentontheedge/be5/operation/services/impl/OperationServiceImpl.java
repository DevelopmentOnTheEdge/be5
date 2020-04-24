package com.developmentontheedge.be5.operation.services.impl;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.operation.validation.Validator;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.operation.OperationConstants.RELOAD_CONTROL_NAME;


public class OperationServiceImpl implements OperationService
{
    public static final Logger log = Logger.getLogger(OperationServiceImpl.class.getName());

    private final OperationExecutor operationExecutor;
    private final Validator validator;

    @Inject
    public OperationServiceImpl(OperationExecutor operationExecutor, Validator validator)
    {
        this.operationExecutor = operationExecutor;
        this.validator = validator;
    }

    @Override
    public Either<Object, OperationResult> generate(Operation operation, Map<String, Object> values)
    {
        Object parameters = operationExecutor.generate(operation, values);

        if (OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        if (parameters == null)
        {
            return execute(operation, values);
        }

        if (values.containsKey(RELOAD_CONTROL_NAME))
        {
            if (parameters instanceof DynamicPropertySet)
            {
                String reloadControlName = ((String) values.get(RELOAD_CONTROL_NAME)).substring(1);
                DynamicProperty property = ((DynamicPropertySet) parameters).getProperty(reloadControlName);
                validator.validate(property);
            }
        }

        return replaceNullValueToEmptyStringAndReturn(parameters);
    }

    @Override
    public Either<Object, OperationResult> execute(Operation operation, Map<String, Object> values)
    {
        Object parameters = operationExecutor.execute(operation, values);

        if (OperationStatus.EXECUTE == operation.getStatus())
        {
            operation.setResultGoBack();
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
                return replaceNullValueToEmptyStringAndReturn(parameters);
            }

            Operation newOperation = operationExecutor.create(operation.getInfo(), operation.getContext());
            Object newParameters = operationExecutor.generate(newOperation, values);

            if (newParameters != null && OperationStatus.ERROR != newOperation.getStatus())
            {
                return replaceNullValueToEmptyStringAndReturn(newParameters);
            }

            return Either.second(operation.getResult());
        }

        return Either.second(operation.getResult());
    }

    private Either<Object, OperationResult> replaceNullValueToEmptyStringAndReturn(Object parameters)
    {
        replaceValuesToString(parameters);
        return Either.first(parameters);
    }

    void replaceValuesToString(Object parameters)
    {
        if (parameters instanceof DynamicPropertySet)
        {
            for (DynamicProperty property : (DynamicPropertySet) parameters)
            {
                if (property.getValue() instanceof DynamicPropertySet)
                {
                    replaceValuesToString(property.getValue());
                }
                if (property.getValue() == null)
                {
                    property.setValue("");
                }
                else if (property.getValue().getClass() != String.class &&
                        property.getValue().getClass() != Boolean.class &&
                        !(property.getValue() instanceof Object[]) &&
                        !(property.getValue() instanceof DynamicPropertySet))
                {
                    property.setValue(property.getValue().toString());
                }
            }
        }
    }
}
