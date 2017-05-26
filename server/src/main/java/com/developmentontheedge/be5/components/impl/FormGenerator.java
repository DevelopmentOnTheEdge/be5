package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperation;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperationFactory;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyUrlsService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.base.Splitter;

import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

public class FormGenerator
{

    private final ServiceProvider serviceProvider = ServerModules.getServiceProvider();

    public FormGenerator(){}

    /**
     * This can generate an usual form or a view parameters (parametrizing
     * operation's form). Parameters:
     * <ul>
     * <li>category</li>
     * <li>page</li>
     * <li>action</li>
     * <li>selectedRows?</li>
     * <li>values?</li>
     * </ul>
     * @see Query#getParametrizingOperation()
     */
    public Either<FormPresentation, FrontendAction> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);
        Operation operation = UserAwareMeta.get(serviceProvider).getOperation(entityName, queryName, operationName);

        return generate(req, entityName, queryName, operationName, selectedRowsString, operation,
                presetValues);
    }

    public FormPresentation generateForm(Request req)
    {
        Either<FormPresentation, FrontendAction> result = generate(req);

        if (!result.isFirst())
        {
            // Bad operation implementation.
            throw new IllegalStateException();
        }

        return result.getFirst();
    }

    /**
     * Redirects the request to the form generation.
     * @param presetValues
     */
    public Either<FormPresentation, FrontendAction> generate(
            String entityName, String operationName, Operation operation,
            Map<String, String> presetValues, Request req)
    {
        checkNotNull(entityName);
        checkNotNull(operationName);
        checkNotNull(operation);
        checkNotNull(req);

        return generate(req, entityName, "", operationName, "", operation, presetValues);
    }

    public Either<FormPresentation, FrontendAction> generate(
            String entityName, String queryName, String operationName, Operation operation,
            Map<String, String> presetValues, Request req)
    {
        checkNotNull(entityName);
        checkNotNull(queryName);
        checkNotNull(operationName);
        checkNotNull(operation);
        checkNotNull(req);

        return generate(req, entityName, queryName, operationName, "", operation, presetValues);
    }

    /**
     * The base method to generate form.
     * @param presetValues
     */
    private Either<FormPresentation, FrontendAction> generate(Request req, String entityName, String queryName,
                                                              String operationName, String selectedRowsString, Operation operation, Map<String, String> presetValues)
    {
        UserAwareMeta userAwareMeta = UserAwareMeta.get(serviceProvider);

        Iterable<String> selectedRows = Splitter.on(',').split(selectedRowsString);

        UserInfo ui = UserInfoHolder.getUserInfo();
        LegacyOperationFactory legacyOperationFactory = new LegacyOperationFactory(req.getRawRequest());//serviceProvider.get(LegacyOperationsService.class).createFactory(ui, req.getRawRequest());
        LegacyOperation legacyOperation = legacyOperationFactory.create(operation, req, "Utils.readQueryID", selectedRows);
        DynamicPropertySet dps = legacyOperation.getParameters(new StringWriter(), presetValues);

        if (dps.size() == 0) // => need no parameter, run the operation immediately
        {
            return Either.second(
                    new OperationExecutor(serviceProvider).execute(legacyOperation, presetValues, req));
        }

        LegacyUrlsService legacyUrlsService = serviceProvider.get(LegacyUrlsService.class);
        Optional<String> customAction = legacyOperation.getCustomAction().map(legacyUrlsService::modernize).map(HashUrl::toString);

        String title = userAwareMeta.getLocalizedOperationTitle(entityName, operationName);

        return Either.first(new FormPresentation(title, selectedRowsString, JsonFactory.dps(dps), presetValues));
    }


}
