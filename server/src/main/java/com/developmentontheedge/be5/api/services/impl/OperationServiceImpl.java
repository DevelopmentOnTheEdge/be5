package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
import com.developmentontheedge.be5.operation.GOperationSupport;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;


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
    public Either<FormPresentation, OperationResult> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, Object> presetValues = req.getValuesFromJson(RestApiConstants.VALUES);
        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);

        return generate(meta, presetValues, selectedRowsString, req);
    }

    @Override
    public Either<FormPresentation, OperationResult> generate(OperationInfo meta,
                Map<String, Object> presetValues, String selectedRowsString, Request req)
    {
        Operation operation = operationExecutor.create(meta, selectedRows(selectedRowsString), req);

        return callGetParameters(operation, presetValues);
    }

    private Either<FormPresentation, OperationResult> callGetParameters(
            Operation operation, Map<String, Object> presetValues)
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
                return form(operation, parameters, OperationResult.error(e));
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
                    return form(operation, parameters, OperationResult.error(e));
                }
            }
        }

        return form(operation, parameters);
    }

    @Override//todo move to component
    public Either<FormPresentation, OperationResult> execute(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, Object> presetValues = req.getValuesFromJson(RestApiConstants.VALUES);

        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);

        return execute(meta, presetValues, selectedRowsString, req);
    }

    @Override
    public Either<FormPresentation, OperationResult> execute(OperationInfo meta,
          Map<String, Object> presetValues, String selectedRowsString, Request req)
    {
        OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), meta.getQueryName());
        Operation operation = operationExecutor.create(meta, selectedRows(selectedRowsString), req);

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

    private Either<FormPresentation, OperationResult> callOperation(
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
                return form(operation, parameters, OperationResult.error(e));
            }
        }

        return callInvoke(presetValues, operation, parameters, operationContext);
    }

    private Either<FormPresentation, OperationResult> callInvoke(
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
                    return form(operation, parameters, OperationResult.error(e));
                }
            }

            if(OperationStatus.ERROR == operation.getStatus() && parameters != null)
            {
                OperationResult invokeResult = operation.getResult();
                Object newParameters = operationExecutor.generate(operation, presetValues);
                return form(operation, newParameters, invokeResult);
            }
        }

        return Either.second(operation.getResult());
    }

    public static String[] selectedRows(String selectedRowsString)
    {
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }

    private Either<FormPresentation, OperationResult> form(Operation operation, Object parameters)
    {
        return form(operation, parameters, operation.getResult());
    }

    private Either<FormPresentation, OperationResult> form(Operation operation, Object parameters, OperationResult operationResult)
    {
        validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

        return Either.first(new FormPresentation(operation.getInfo(),
                userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                Arrays.stream(operation.getRecords()).collect(Collectors.joining(",")),
                JsonFactory.bean(parameters), operation.getLayout(),
                operationResult));
    }

}
