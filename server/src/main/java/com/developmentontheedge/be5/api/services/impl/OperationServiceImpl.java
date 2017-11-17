package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.FrontendConstants;
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
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Map;
import static com.google.common.base.Strings.nullToEmpty;


public class OperationServiceImpl implements OperationService
{
    private final Injector injector;
    private final OperationExecutor operationExecutor;
    private final DatabaseService databaseService;
    private final UserAwareMeta userAwareMeta;
    private final Validator validator;

    public OperationServiceImpl(Injector injector, OperationExecutor operationExecutor,
                                DatabaseService databaseService, Validator validator, UserAwareMeta userAwareMeta)
    {
        this.injector = injector;
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

    @Override//todo refactoring to return only Object - (DPS or bean)
    public Either<FormPresentation, OperationResult> generate(OperationInfo meta,
                Map<String, Object> presetValues, String selectedRowsString, Request req)
    {
        Operation operation = operationExecutor.create(meta, selectedRows(selectedRowsString), req);

        return callGetParameters(selectedRowsString, operation, presetValues);
    }

    private Either<FormPresentation, OperationResult> callGetParameters(
            String selectedRowsString, Operation operation, Map<String, Object> presetValues)
    {
        OperationResult invokeResult = null;
        if(OperationStatus.ERROR == operation.getStatus())
        {
            invokeResult = operation.getResult();
            operation.setResult(OperationResult.open());
        }

        Object parameters = operationExecutor.generate(operation, presetValues);

        if(invokeResult != null && invokeResult.getStatus() == OperationStatus.ERROR)
        {
            validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

            return Either.first(new FormPresentation(operation.getInfo(),
                    userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                    selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
                    invokeResult));
        }

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
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                return Either.first(new FormPresentation(operation.getInfo(),
                        userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
                        OperationResult.error(e)));
            }
        }

        //run manually in component
        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), operation.getInfo().getQueryName());
            if(operation instanceof TransactionalOperation)
            {
                return databaseService.transaction((connection) ->
                        callInvoke(selectedRowsString, presetValues, operation,
                                null, operationContext)
                );
            }
            else
            {
                return callInvoke(selectedRowsString, presetValues, operation,
                        null, operationContext);
            }
        }

        OperationResult operationResult = OperationResult.open();
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
                    operationResult = OperationResult.error(e);
                }
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);//todo add test, refactoring:
                // create function getEitherFirstFormPresentation(){replaceNullValueToEmptyString; Either.first(new FormPresentation;}
            }
            else
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);
            }
        }

        return Either.first(new FormPresentation(operation.getInfo(),
                userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), operationResult));
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

    @Override//todo refactoring to Either<Object, OperationResult> Object - (DPS or bean)
    public Either<FormPresentation, OperationResult> execute(OperationInfo meta,
          Map<String, Object> presetValues, String selectedRowsString, Request req)
    {
        OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), meta.getQueryName());
        Operation operation = operationExecutor.create(meta, selectedRows(selectedRowsString), req);

        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transaction(connection ->
                    callOperation(selectedRowsString, presetValues, operation, operationContext)
            );
        }
        else
        {
            return callOperation(selectedRowsString, presetValues, operation, operationContext);
        }
    }

    private Either<FormPresentation, OperationResult> callOperation(
            String selectedRowsString, Map<String, Object> presetValues, Operation operation,
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
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                return Either.first(new FormPresentation(operation.getInfo(),
                        userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
                        OperationResult.error(e)));
            }
        }

        return callInvoke(selectedRowsString, presetValues, operation, parameters, operationContext);
    }

    private Either<FormPresentation, OperationResult> callInvoke(
            String selectedRowsString, Map<String, Object> presetValues, Operation operation,
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
                    validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                    return Either.first(new FormPresentation(operation.getInfo(),
                            userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                            selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
                            OperationResult.error(e)));
                }
            }

            if(OperationStatus.ERROR == operation.getStatus() && parameters != null)
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                return callGetParameters(selectedRowsString, operation, presetValues);
            }
        }

        return Either.second(operation.getResult());
    }

    public static String[] selectedRows(String selectedRowsString)
    {
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }
}
