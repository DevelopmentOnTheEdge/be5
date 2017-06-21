package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.Validator;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Map;
import java.util.stream.StreamSupport;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.google.common.base.Strings.nullToEmpty;

public class OperationServiceImpl implements OperationService
{
    private final Injector injector;
    private final UserAwareMeta userAwareMeta;

    public OperationServiceImpl(Injector injector) {
        this.injector = injector;
        userAwareMeta = UserAwareMeta.get(injector);
    }

    @Override
    public Either<FormPresentation, OperationResult> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);
        OperationInfo operationInfo = UserAwareMeta.get(injector).getOperation(entityName, queryName, operationName);

        return generate(entityName, queryName, operationName, selectedRowsString, operationInfo,
                presetValues, req);
    }

    private Either<FormPresentation, OperationResult> generate(String entityName, String queryName,
             String operationName, String selectedRowsString, OperationInfo meta, Map<String, String> presetValues, Request req)
    {
        Operation operation = create(meta);

        Object parameters;
        try
        {
            parameters = operation.getParameters(presetValues);
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }

        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);
            return execute(operation, null, operationContext, req);
        }

        if(parameters instanceof DynamicPropertySet)
        {
            if(presetValues.containsKey(OperationSupport.reloadControl))
            {
                Validator.checkAndCast(((OperationSupport)operation).dps);
            }
            else
            {
                Validator.replaceNullValueToStr((DynamicPropertySet) parameters);
            }
        }

        return Either.first(new FormPresentation(entityName, queryName, operationName,
                userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                selectedRowsString, JsonFactory.bean(parameters), presetValues));
    }

    @Override
    public Either<FormPresentation, OperationResult> execute(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);

        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);
        OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);

        Operation operation = create(meta);

        Object parameters;
        try
        {
            parameters = operation.getParameters(presetValues);
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }

        if(parameters instanceof DynamicPropertySet)
        {
            ((OperationSupport)operation).dps = (DynamicPropertySet) parameters;

            Validator.checkAndCast((DynamicPropertySet) parameters);

            if(StreamSupport.stream(((DynamicPropertySet)parameters).spliterator(), false).anyMatch(Validator::isError))
            {
                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), presetValues));
            }
        }

        execute(operation, parameters, operationContext, req);

        return Either.second(operation.getResult());
    }

    public Either<FormPresentation, OperationResult> execute(Operation operation, Object parameters, OperationContext operationContext, Request req)
    {
        try
        {
            operation.invoke(parameters, operationContext);

            if(operation.getResult().getStatus() == OperationStatus.IN_PROGRESS)
            {
                operation.setResult(OperationResult.redirect(
                    new HashUrl(FrontendConstants.TABLE_ACTION,
                        req.get(RestApiConstants.ENTITY),
                        req.get(RestApiConstants.QUERY))
                ));
            }

            return Either.second(operation.getResult());
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }
    }

    @Override
    public Operation create(Operation operation) {
        operation.initialize(injector, null, OperationResult.progress());

        return operation;
    }

    @Override
    public Operation create(OperationInfo operationInfo) {
        Operation operation;
        String code = operationInfo.getCode();

        switch (operationInfo.getType())
        {
            case OPERATION_TYPE_GROOVY:
                try
                {
                    operation = ( Operation ) GroovyRegister.parseClass( code ).newInstance();
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                break;
            default:
                try {
                    operation = ( Operation ) Class.forName(code).newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw Be5Exception.internalInOperation(e, operationInfo);
                }
        }

        operation.initialize(injector, operationInfo, OperationResult.progress());

        return operation;
    }

    Long[] selectedRows(String selectedRowsString){
        if(selectedRowsString.trim().isEmpty())return new Long[0];

        String[] split = selectedRowsString.split(",");
        Long longs[] = new Long[split.length];
        for (int i=0 ;i < split.length; i++){
            longs[i] = Long.parseLong(split[i]);
        }
        return longs;
    }
}
