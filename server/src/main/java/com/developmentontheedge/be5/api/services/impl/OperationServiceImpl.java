package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.Be5Caches;
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
import com.github.benmanes.caffeine.cache.Cache;

import java.util.Map;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.google.common.base.Strings.nullToEmpty;


public class OperationServiceImpl implements OperationService
{
    private final Cache<String, Class> groovyOperationClasses;
    private final Injector injector;
    private final UserAwareMeta userAwareMeta;
    //private final DpsHelper dpsHelper;
    private final Validator validator;

    public OperationServiceImpl(Injector injector, Validator validator, Be5Caches be5Caches, UserAwareMeta userAwareMeta)
    {
        this.injector = injector;
        this.validator = validator;
        this.userAwareMeta = userAwareMeta;
        //this.dpsHelper = dpsHelper;

        groovyOperationClasses = be5Caches.createCache("Groovy operation classes");
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
        Operation operation = create(meta, selectedRows(selectedRowsString), req);

        Object parameters = getParametersFromOperation(operation, presetValues);

        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);
            return execute(entityName, queryName, operationName, selectedRowsString,
                    presetValues, operation, null, operationContext, req);
        }

        String errorMsg = "";
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
                    errorMsg = e.getMessage() + " - " + e.toString();
                }
            }
            else
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);
            }
        }

        return Either.first(new FormPresentation(entityName, queryName, operationName,
                userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues, errorMsg));
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

        Operation operation = create(meta, selectedRows(selectedRowsString), req);

        //add TransactionalOperation interface and support all in transaction getParametersFromOperation and invoke in execute

        Object parameters = getParametersFromOperation(operation, presetValues);

        if(parameters instanceof DynamicPropertySet)
        {
            ((OperationSupport)operation).dps = (DynamicPropertySet) parameters;
            try {
                validator.checkErrorAndCast(((OperationSupport)operation).dps);
            }catch (RuntimeException e){
                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                        e.getMessage() + " - " + e.toString()));
            }
        }

        //todo add tests
//        if (parameters instanceof DynamicPropertySet)
//        {
//            DynamicPropertySet dps = (DynamicPropertySet)parameters;
//            dpsHelper.addSpecialIfNotExists(dps, meta.getEntity());
//            dpsHelper.setSpecialPropertyIfNull(dps);
//        }

        return execute(entityName, queryName, operationName, selectedRowsString,
                presetValues, operation, parameters, operationContext, req);
    }

    public Either<FormPresentation, OperationResult> execute(String entityName, String queryName, String operationName,
         String selectedRowsString, Map<String, Object> presetValues, Operation operation,
                                                             Object parameters, OperationContext operationContext, Request req)
    {
        try
        {
            operation.invoke(parameters, operationContext);

            if(operation.getResult().getStatus() == OperationStatus.ERROR)
            {
                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                        operation.getResult().getMessage() + " " + operation.getResult().getDetails().toString()));
            }

            if (parameters instanceof DynamicPropertySet) {
                try {
                    validator.isError((DynamicPropertySet) parameters);
                } catch (RuntimeException e) {
                    return Either.first(new FormPresentation(entityName, queryName, operationName,
                            userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                            selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                            e.getMessage() + " - " + e.toString()));
                }
            }

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

    public Operation create(OperationInfo operationInfo, String[] records, Request request)
    {
        Operation operation;

        switch (operationInfo.getType())
        {
            case OPERATION_TYPE_GROOVY:
                try
                {
                    Class aClass = groovyOperationClasses.get(operationInfo.getEntity() + operationInfo.getName(),
                            k -> GroovyRegister.parseClass( operationInfo.getCode() ));
                    if(aClass != null)
                    {
                        operation = ( Operation ) aClass.newInstance();
                    }
                    else
                    {
                        throw Be5Exception.internal("Class " + operationInfo.getCode() + " is null." );
                    }
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                break;
            default:
                try {
                    operation = ( Operation ) Class.forName(operationInfo.getCode()).newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw Be5Exception.internalInOperation(e, operationInfo);
                }
        }

        operation.initialize(operationInfo, OperationResult.progress(), records, request);
        injector.injectAnnotatedFields(operation);

        return operation;
    }

    private Object getParametersFromOperation(Operation operation, Map<String, Object> presetValues)
    {
        try
        {
            Object parameters = operation.getParameters(presetValues);
//            if (parameters instanceof DynamicPropertySet)
//            {
//                dpsHelper.setValuesAndAddColumns(operation.getInfo().getEntity(), (DynamicPropertySet)parameters, presetValues);
//            }
            return parameters;
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }
    }

    static String[] selectedRows(String selectedRowsString)
    {
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }
}
