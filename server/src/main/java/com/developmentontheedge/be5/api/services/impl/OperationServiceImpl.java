package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.validation.Validator;
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
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import java.util.Map;
import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.google.common.base.Strings.nullToEmpty;


public class OperationServiceImpl implements OperationService
{
    private final Injector injector;
    private final DatabaseService databaseService;
    private final UserAwareMeta userAwareMeta;
    private final Validator validator;
    private final GroovyOperationLoader groovyOperationLoader;


    public OperationServiceImpl(GroovyOperationLoader groovyOperationLoader, Injector injector,
                                DatabaseService databaseService, Validator validator, UserAwareMeta userAwareMeta)
    {
        this.injector = injector;
        this.databaseService = databaseService;
        this.validator = validator;
        this.userAwareMeta = userAwareMeta;
        this.groovyOperationLoader = groovyOperationLoader;
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
        Operation operation = create(meta, selectedRows(selectedRowsString), req);

        return callGetParameters(entityName, queryName, operationName, selectedRowsString, operation, meta,
                presetValues, req);
    }

    private Either<FormPresentation, OperationResult> callGetParameters(String entityName, String queryName,
             String operationName, String selectedRowsString, Operation operation, OperationInfo meta,
                                                                        Map<String, Object> presetValues, Request req)
    {
        OperationResult invokeResult = null;
        if(OperationStatus.ERROR == operation.getStatus())
        {
            invokeResult = operation.getResult();
            operation.setResult(OperationResult.open());
        }

        Object parameters = getParametersFromOperation(operation, presetValues);

        if(invokeResult != null && invokeResult.getStatus() == OperationStatus.ERROR)
        {
            validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);

            return Either.first(new FormPresentation(entityName, queryName, operationName,
                    userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
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

                return Either.first(new FormPresentation(entityName, queryName, operationName,
                        userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
                        OperationResult.error(e)));
            }
        }

        if (parameters == null)
        {
            OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);
            if(operation instanceof TransactionalOperation)
            {
                return databaseService.transaction((connection) ->
                        callInvoke(entityName, queryName, operationName, selectedRowsString,
                                presetValues, operation, meta, null, operationContext, req)
                );
            }
            else
            {
                return callInvoke(entityName, queryName, operationName, selectedRowsString,
                                  presetValues, operation, meta, null, operationContext, req);
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
            }
            else
            {
                validator.replaceNullValueToEmptyString((DynamicPropertySet) parameters);
            }
        }

        return Either.first(new FormPresentation(entityName, queryName, operationName,
                userAwareMeta.getLocalizedOperationTitle(entityName, operationName),
                selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(), operationResult));
    }

    @Override
    public Either<FormPresentation, OperationResult> execute(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, Object> presetValues = req.getValuesFromJson(RestApiConstants.VALUES);

        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);
        OperationContext operationContext = new OperationContext(selectedRows(selectedRowsString), queryName);

        Operation operation = create(meta, selectedRows(selectedRowsString), req);

        if(operation instanceof TransactionalOperation)
        {
            return databaseService.transaction((connection) ->
                    callOperation(entityName, queryName, operationName, selectedRowsString,
                                  presetValues, operation, meta, operationContext, req)
            );
        }
        else
        {
            return callOperation(entityName, queryName, operationName, selectedRowsString,
                    presetValues, operation, meta, operationContext, req);
        }
    }

    private Either<FormPresentation, OperationResult> callOperation(String entityName, String queryName, String operationName,
                                                                 String selectedRowsString, Map<String, Object> presetValues, Operation operation, OperationInfo meta,
                                                                 OperationContext operationContext, Request req)
    {
        Object parameters = getParametersFromOperation(operation, presetValues);

        if(OperationStatus.ERROR == operation.getStatus())
        {
            return Either.second(operation.getResult());
        }

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
                        selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
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
                            selectedRowsString, JsonFactory.bean(parameters), operation.getLayout(),
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
        catch (Be5Exception e)
        {
            throw e;//Be5Exception.internalInOperation(e.getCause(), operation.getInfo())
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
                    Class aClass = groovyOperationLoader.get(operationInfo);
                    if(aClass != null)
                    {
                        operation = ( Operation ) aClass.newInstance();
                    }
                    else
                    {
                        throw Be5Exception.internalInOperation(
                                new Error("Class " + operationInfo.getCode() + " is null."), operationInfo);
                        //throw Be5Exception.internal("Class " + operationInfo.getCode() + " is null." );
                    }
                }
                catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
                {
                    throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
                }
                catch( MultipleCompilationErrorsException e )
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

    public static String[] selectedRows(String selectedRowsString)
    {
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }
}
