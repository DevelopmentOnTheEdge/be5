package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.MoreRows;
import com.developmentontheedge.be5.query.model.MoreRowsBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.support.ApiControllerSupport;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class TableController extends ApiControllerSupport
{
    private static final Logger log = Logger.getLogger(TableController.class.getName());

    private final DocumentGenerator documentGenerator;
    private final TableModelService tableModelService;
    private final UserAwareMeta userAwareMeta;
    private final JsonApiResponseHelper responseHelper;

    @Inject
    public TableController(DocumentGenerator documentGenerator, TableModelService tableModelService,
                           UserAwareMeta userAwareMeta, JsonApiResponseHelper responseHelper)
    {
        this.documentGenerator = documentGenerator;
        this.tableModelService = tableModelService;
        this.userAwareMeta = userAwareMeta;
        this.responseHelper = responseHelper;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);

        Map<String, Object> parameters = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));

        switch (requestSubUrl)
        {
            case "":
                JsonApiModel jsonApiForUser = getQueryJsonApiForUser(entityName, queryName, parameters);
                jsonApiForUser.setMeta(responseHelper.getDefaultMeta(req));
                res.sendAsJson(jsonApiForUser);
                return;
            case "update":
                res.sendAsJson(getUpdateQueryJsonApiForUser(entityName, queryName, parameters));
                return;
            default:
                responseHelper.sendUnknownActionError();
        }
    }

    JsonApiModel getQueryJsonApiForUser(String entityName, String queryName, Map<String, Object> parameters)
    {
        try
        {
            Query query = userAwareMeta.getQuery(entityName, queryName);
            TableModel tableModel = tableModelService.getTableModel(query, parameters);

            return documentGenerator.getJsonApiModel(query, parameters, tableModel);
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters);
            log.log(Level.SEVERE, "Error in table" + url.toString(), e);
            return JsonApiModel.error(responseHelper.
                    getErrorModel(e, "", Collections.singletonMap(SELF_LINK, url.toString())), null);
        }
    }

    //todo refactor frontend to JsonApiModel
    Object getUpdateQueryJsonApiForUser(String entityName, String queryName, Map<String, Object> parameters)
    {
        try
        {
            Query query = userAwareMeta.getQuery(entityName, queryName);
            TableModel tableModel = tableModelService.getTableModel(query, parameters);

            return new MoreRows(
                    tableModel.getTotalNumberOfRows().intValue(),
                    tableModel.getTotalNumberOfRows().intValue(),
                    new MoreRowsBuilder(tableModel).build()
            );
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters);
            log.log(Level.SEVERE, "Error in table" + url.toString(), e);
            return JsonApiModel.error(responseHelper.
                    getErrorModel(e, "", Collections.singletonMap(SELF_LINK, url.toString())), null);
        }
    }

}
