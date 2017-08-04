package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.Validator;
import com.developmentontheedge.be5.api.helpers.SqlHelper;
import com.developmentontheedge.be5.api.services.CacheInfo;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.google.common.base.Strings.nullToEmpty;

public class OperationServiceImpl implements OperationService
{
    private final Cache<String, Class> groovyOperationClasses;
    private final Injector injector;
    private final UserAwareMeta userAwareMeta;
    private final SqlHelper sqlHelper;
    private final Validator validator;

    public OperationServiceImpl(Injector injector)
    {
        this.injector = injector;
        this.validator = injector.get(Validator.class);
        this.userAwareMeta = injector.get(UserAwareMeta.class);
        sqlHelper = injector.get(SqlHelper.class);

        groovyOperationClasses = Caffeine.newBuilder()
                .maximumSize(10_000)
                .recordStats()
                .build();
        CacheInfo.registerCache("Groovy operation classes", groovyOperationClasses);
    }

    @Override
    public Either<FormPresentation, OperationResult> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, Object> presetValues = req.getValues(RestApiConstants.VALUES);
        OperationInfo operationInfo = userAwareMeta.getOperation(entityName, queryName, operationName);

        return generate(entityName, queryName, operationName, selectedRowsString, operationInfo,
                presetValues, req);
    }

    private Either<FormPresentation, OperationResult> generate(String entityName, String queryName,
             String operationName, String selectedRowsString, OperationInfo meta, Map<String, Object> presetValues, Request req)
    {
        Operation operation = create(meta, selectedRows(selectedRowsString));

        Object parameters = getParametersAndSetValueIfNull(operation, meta.getEntity(), presetValues);

        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);
            return execute(operation, null, operationContext, req);
        }

        if(parameters instanceof DynamicPropertySet)
        {
            if(presetValues.containsKey(OperationSupport.reloadControl))
            {
                try
                {
                    validator.checkErrorAndCast((DynamicPropertySet) parameters);
                }
                catch (RuntimeException ignore)
                {

                }
            }
            else
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);
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
        Map<String, Object> presetValues = req.getValues(RestApiConstants.VALUES);

        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);
        OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);

        Operation operation = create(meta, selectedRows(selectedRowsString));

        Object parameters = getParametersAndSetValueIfNull(operation, meta.getEntity(), presetValues);

        if(parameters instanceof DynamicPropertySet)
        {
            ((OperationSupport)operation).dps = (DynamicPropertySet) parameters;
            try {
                validator.checkErrorAndCast((DynamicPropertySet) parameters);
            }catch (RuntimeException e){
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
//
//    public Operation create(Operation operation) {
//        operation.initialize(injector, null, OperationResult.progress());
//
//        return operation;
//    }

    public Operation create(OperationInfo operationInfo, String[] records) {
        Operation operation;

        switch (operationInfo.getType())
        {
            case OPERATION_TYPE_GROOVY:
                try
                {
                    Class aClass = groovyOperationClasses.get(operationInfo.getEntity() + operationInfo.getName(),
                            k -> GroovyRegister.parseClass( operationInfo.getCode() ));
                    operation = ( Operation ) aClass.newInstance();
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                catch (Throwable e)
                {
                    throw Be5Exception.internalInOperation(e, operationInfo);
                }
                break;
            default:
                try {
                    operation = ( Operation ) Class.forName(operationInfo.getCode()).newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw Be5Exception.internalInOperation(e, operationInfo);
                }
        }

        operation.initialize(injector, operationInfo, OperationResult.progress(), records);

        return operation;
    }

    /**
     * Либо значение не задаётся и оно будет автоматически подставлятся из presetValues после выполнения getParameters,
     * либо вы задаёте значение и вручную управляете её изменением в getParameters:
     * see com.developmentontheedge.be5.operations.TestOperationProperty in tests
     */
    private Object getParametersAndSetValueIfNull(Operation operation, Entity entity, Map<String, Object> presetValues) {
        try
        {
            Object parameters = operation.getParameters(presetValues);
            if (parameters instanceof DynamicPropertySet)
            {
                sqlHelper.setValues((DynamicPropertySet)parameters, entity, presetValues);
            }
            return parameters;
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }
    }

    String[] selectedRows(String selectedRowsString){
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }
}
