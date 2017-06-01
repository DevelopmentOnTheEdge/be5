package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyUrlParser;
import com.developmentontheedge.be5.api.operationstest.v1.OperationRequest;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

public class OperationServiceImpl implements OperationService
{
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(OperationServiceImpl.class.getName());

    private final ServiceProvider serviceProvider;

    public OperationServiceImpl(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public Either<FormPresentation, FrontendAction> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);
        OperationInfo operationInfo = UserAwareMeta.get(serviceProvider).getOperation(entityName, queryName, operationName);

        return generate(entityName, queryName, operationName, selectedRowsString, operationInfo,
                presetValues);
    }

    private Either<FormPresentation, FrontendAction> generate(String entityName, String queryName,
                                                              String operationName, String selectedRowsString, OperationInfo meta, Map<String, String> presetValues)
    {
        UserAwareMeta userAwareMeta = UserAwareMeta.get(serviceProvider);

        Iterable<String> selectedRows = Splitter.on(',').split(selectedRowsString);

        String[] records = Iterables.toArray(selectedRows, String.class);
        OperationContext operationContext = new OperationContext(records);
        Operation operation = create(meta, operationContext);

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
            return Either.second(execute(operation, presetValues));
        }

        String title = userAwareMeta.getLocalizedOperationTitle(entityName, operationName);

        return Either.first(new FormPresentation(title, selectedRowsString, JsonFactory.bean(parameters), presetValues));
    }

    @Override
    public FrontendAction execute(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        Iterable<String> selectedRows = Splitter.on(',').split(req.getOrEmpty(RestApiConstants.SELECTED_ROWS));
        Map<String, String> parameters = req.getValues(RestApiConstants.VALUES);

        OperationInfo meta = UserAwareMeta.get(serviceProvider).getOperation(entityName, queryName, operationName);
        String[] records = Iterables.toArray(selectedRows, String.class);
        OperationContext operationContext = new OperationContext(records);

        Operation operation = create(meta, operationContext);



        execute(operation, parameters);

        return operation.getResult().getFrontendAction();
    }

    public FrontendAction execute(Operation operation, Map<String, String> parameters)
    {
        try
        {
            operation.invoke(parameters);
            return operation.getResult().getFrontendAction();
        }
        catch (Exception e)
        {
            throw Be5Exception.internalInOperation(e, operation.getInfo());
        }
    }

    private FrontendAction formModernRedirectUrl(Operation operation, Request req)
    {
        if(operation.equals(LegacyUrlParser.GO_BACK_URL))
            return FrontendAction.goBack();

        return FrontendAction.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, req.get(RestApiConstants.ENTITY), req.get(RestApiConstants.QUERY)).named(new OperationRequest(req).getAll()));
    }

    public Operation create(OperationInfo meta, OperationContext operationContext) {
        Operation operation;

        switch (meta.getType())
        {
//        case DatabaseConstants.OP_TYPE_SQL:
//            if (user.isAdmin())
//            {
//                legacyOperation = new SQLOperation();
//            }
//            else
//            {
//                legacyOperation = new SilentSqlOperation();
//            }
//            ((SQLOperation) legacyOperation).setCode(code);
//            break;
//        case DatabaseConstants.OP_TYPE_JAVASCRIPT_SERVER:
//            if (JavaScriptOperation.canBeOffline(code))
//            {
//                legacyOperation = new OfflineJavaScriptOperation();
//            }
//            else
//            {
//                legacyOperation = new JavaScriptOperation();
//            }
//            ((JavaScriptOperation) legacyOperation).setCode(code);
//            break;
//        case DatabaseConstants.OP_TYPE_JAVA_FUNCTION:
//            legacyOperation = new MethodWrapperOperation();
//            ((MethodWrapperOperation) legacyOperation).setCode(code);
//            break;
            default:
                try {
                    Class<?> aClass = Class.forName(meta.getCode());
                    operation = (Operation)aClass.newInstance();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw Be5Exception.internalInOperation(e, meta);
                }
                break;
        }

        operation.initialize(serviceProvider, meta, operationContext, OperationResult.progress());

        return operation;
    }

}
