package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Level;

import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;


public class TableController extends ApiControllerSupport
{
    private final DocumentGenerator documentGenerator;
    private final JsonApiResponseHelper responseHelper;

    @Inject
    public TableController(DocumentGenerator documentGenerator, JsonApiResponseHelper responseHelper)
    {
        this.documentGenerator = documentGenerator;
        this.responseHelper = responseHelper;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);

        Map<String, Object> parameters = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));

        try
        {
            switch(requestSubUrl)
            {
                case "":
                    JsonApiModel jsonApiForUser = documentGenerator.queryJsonApiFor(entityName, queryName, parameters);
                    jsonApiForUser.setMeta(responseHelper.getDefaultMeta(req));
                    res.sendAsJson(jsonApiForUser);
                    return;
                case "update":
                    res.sendAsJson(documentGenerator.updateQueryJsonApi(entityName, queryName, parameters));
                    return;
                default:
                    responseHelper.sendUnknownActionError(req);
            }
        }
        catch(Be5Exception e)
        {
            String url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters).toString();
            log.log(Level.SEVERE, "Error in table: " + url + ", on requestSubUrl = '" + requestSubUrl + "'", e);
            responseHelper.sendErrorAsJson(e, req);
        }
    }

}
