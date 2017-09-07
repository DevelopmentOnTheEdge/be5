package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
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
    private final Validator validator;

    public OperationServiceImpl(Injector injector, Validator validator, Be5Caches be5Caches, UserAwareMeta userAwareMeta)
    {
        this.injector = injector;
        this.validator = validator;
        this.userAwareMeta = userAwareMeta;

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

        return callGetParameters(entityName, queryName, operationName, selectedRowsString, null, operationInfo,
                presetValues, req);
    }

    private Either<FormPresentation, OperationResult> callGetParameters(String entityName, String queryName,
             String operationName, String selectedRowsString, Operation operation, OperationInfo meta, Map<String, Object> presetValues, Request req)
    {
        OperationResult invokeResult = null;
        if(operation == null)
        {
            operation = create(meta, selectedRows(selectedRowsString), req);
        }
        else
        {
            if(OperationStatus.ERROR == operation.getStatus())
            {
                invokeResult = operation.getResult();
                operation.setResult(OperationResult.open());
            }
        }

        Object parameters = getParametersFromOperation(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

        if(invokeResult != null && invokeResult.getStatus() == OperationStatus.ERROR)
        {
            validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

            return Either.first(new FormPresentation(entityName, queryName, operationName,
                    userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                    selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                    invokeResult));
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

                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                        OperationResult.error(e)));
            }
        }

        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);
            return callInvoke(entityName, queryName, operationName, selectedRowsString,
                    presetValues, operation, meta, null, operationContext, req);
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
                selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues, OperationResult.error(errorMsg)));
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
            try
            {
                validator.checkErrorAndCast(((OperationSupport)operation).dps);
            }
            catch (RuntimeException e)
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                        OperationResult.error(e)));
            }
        }

        return callInvoke(entityName, queryName, operationName, selectedRowsString,
                presetValues, operation, meta, parameters, operationContext, req);
    }

    private Either<FormPresentation, OperationResult> callInvoke(String entityName, String queryName, String operationName,
         String selectedRowsString, Map<String, Object> presetValues, Operation operation, OperationInfo meta,
                                                             Object parameters, OperationContext operationContext, Request req)
    {
        try
        {
            operation.setResult(OperationResult.progress());
            operation.invoke(parameters, operationContext);

            if(OperationStatus.ERROR == operation.getStatus() && parameters != null)
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

                return callGetParameters(entityName, queryName, operationName, selectedRowsString, operation, meta,
                        presetValues, req);
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

                    return Either.first(new FormPresentation(entityName, queryName, operationName,
                            userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                            selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), presetValues,
                            OperationResult.error(e)));
                }
            }

            if(OperationStatus.IN_PROGRESS == operation.getStatus())
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

    private Operation create(OperationInfo operationInfo, String[] records, Request request)
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

        operation.initialize(operationInfo, OperationResult.open(), records, request);
        injector.injectAnnotatedFields(operation);

        return operation;
    }

    private Object getParametersFromOperation(Operation operation, Map<String, Object> presetValues)
    {
        try
        {
            return operation.getParameters(presetValues);
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
