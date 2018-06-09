package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.operation.util.OperationUtils;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.support.ApiControllerSupport;
import com.google.inject.Stage;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;


public class FormController extends ApiControllerSupport
{
    private final DocumentGenerator documentGenerator;
    private final UserHelper userHelper;
    private final JsonApiResponseHelper responseHelper;
    private final Stage stage;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public FormController(DocumentGenerator documentGenerator,
                          UserHelper userHelper, JsonApiResponseHelper responseHelper,
                          UserInfoProvider userInfoProvider, Stage stage)
    {
        this.documentGenerator = documentGenerator;
        this.userHelper = userHelper;
        this.responseHelper = responseHelper;
        this.stage = stage;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        //todo move to filter
        if(stage == Stage.DEVELOPMENT && userInfoProvider.get() == null)
        {
            userHelper.initGuest();
        }

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String[] selectedRows = OperationUtils.selectedRows(nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS)));
        Map<String, Object> operationParams = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.OPERATION_PARAMS));
        Map<String, Object> values = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));

        JsonApiModel jsonApiModel = documentGenerator.getFormJsonApiModel(requestSubUrl,
                                        entityName, queryName, operationName, selectedRows, operationParams, values);

        jsonApiModel.setMeta(responseHelper.getDefaultMeta(req));
        responseHelper.sendAsJson(jsonApiModel);
    }


}
