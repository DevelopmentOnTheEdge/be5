package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.google.common.base.Strings.nullToEmpty;


public class OperationServiceImpl implements OperationService
{
    private final Cache<String, Class> groovyOperationClasses;
    private final Injector injector;
    private final DatabaseService databaseService;
    private final UserAwareMeta userAwareMeta;
    private final Meta meta;
    private final Validator validator;

    private Map<String, com.developmentontheedge.be5.metadata.model.Operation> operationMap;

    public OperationServiceImpl(Injector injector, DatabaseService databaseService, Validator validator, Be5Caches be5Caches, UserAwareMeta userAwareMeta, Meta meta)
    {
        this.injector = injector;
        this.databaseService = databaseService;
        this.validator = validator;
        this.userAwareMeta = userAwareMeta;
        this.meta = meta;

        groovyOperationClasses = be5Caches.createCache("Groovy operation classes");

        initOperationMap();
    }

    @Override
    public void initOperationMap()
    {
        Map<String, com.developmentontheedge.be5.metadata.model.Operation> newOperationMap = new HashMap<>();
        List<Entity> entities = meta.getOrderedEntities("ru");
        for (Entity entity : entities)
        {
            List<String> operationNames = meta.getOperationNames(entity);
            for (String operationName : operationNames)
            {
                com.developmentontheedge.be5.metadata.model.Operation operation = meta.getOperation(entity, operationName, new ArrayList<>(meta.getProjectRoles()));
                switch (operation.getType())
                {
                    case OPERATION_TYPE_GROOVY:
                        GroovyOperation groovyOperation = (GroovyOperation) operation;
                        String fileName = groovyOperation.getFileName().replace("/", ".");
                        newOperationMap.put(fileName, operation);
                        break;
                    default:
                        try
                        {
                            newOperationMap.put(operation.getCode(), operation);
                        }
                        catch (RuntimeException ignore)
                        {
                            // ignore old be3 operation (because copying modules)
                        }
                }
            }
        }

        operationMap = newOperationMap;
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
                    preloadSuperOperation(operationInfo);
                    Class aClass = groovyOperationClasses.get(operationInfo.getEntity() + operationInfo.getName(),
                            k -> GroovyRegister.parseClass( operationInfo.getCode(),
                                    operationInfo.getEntity() + "." + operationInfo.getName() + ".groovy" ));
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

    List<String> preloadSuperOperation(OperationInfo operationInfo)
    {
        String superOperationCanonicalName = getCanonicalName(operationInfo);

        com.developmentontheedge.be5.metadata.model.Operation superOperation = operationMap.get(superOperationCanonicalName);

        if (superOperation != null && superOperation.getType().equals(OPERATION_TYPE_GROOVY))
        {
            //preloadSuperOperation(new OperationInfo("", anyOperation));

            groovyOperationClasses.get(superOperationCanonicalName,
                    k -> GroovyRegister.parseClass(superOperation.getCode(), superOperationCanonicalName));
            return Collections.singletonList(superOperationCanonicalName);
        }

        return Collections.emptyList();
    }

    String getSimpleName(OperationInfo operationInfo)
    {
        GroovyOperation groovyOperation = (GroovyOperation) operationInfo.getModel();
        String fileName = groovyOperation.getFileName();
        String className = fileName.substring(fileName.lastIndexOf("/")+1, fileName.length() - ".groovy".length()).trim();
        String classBegin = "class " + className + " extends ";

        String code = operationInfo.getCode();
        int superClassBeginPos = code.indexOf(classBegin);
        if(superClassBeginPos == -1)return null;

        superClassBeginPos += classBegin.length();
        int superClassEndPos = Math.min(
                code.indexOf(" ", superClassBeginPos) != -1 ? code.indexOf(" ", superClassBeginPos) : 999999999,
                code.indexOf("\n", superClassBeginPos));


        return code.substring(superClassBeginPos, superClassEndPos).trim();
    }

    String getCanonicalName(OperationInfo operationInfo)
    {
        String code = operationInfo.getCode();
        String superOperationName = getSimpleName(operationInfo);

        String superOperationFullName = superOperationName + ".groovy";

        int lineBegin = code.indexOf("package ");
        if(lineBegin != -1){
            int lineEnd = code.indexOf("\n", lineBegin);
            String line = code.substring(lineBegin, lineEnd);
            superOperationFullName = line.replace("package ", "").replace(";", "")
                    + "." + superOperationName + ".groovy";
        }

        lineBegin = code.indexOf("import ");
        while (lineBegin != -1)
        {
            int lineEnd = code.indexOf("\n", lineBegin);
            String line = code.substring(lineBegin, lineEnd);
            if(line.contains("." + superOperationName)){
                superOperationFullName = line.replace("import ", "").replace(";", "") + ".groovy";
            }
            lineBegin = code.indexOf("import ", lineEnd);
        }
        return superOperationFullName;
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
