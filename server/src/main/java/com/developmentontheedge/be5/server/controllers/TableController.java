package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.metadata.QueryType;
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

        HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters);

        Query query;
        try
        {
            query = userAwareMeta.getQuery(entityName, queryName);
        }
        catch (Be5Exception e)
        {
            sendError(req, res, url, e);
            return;
        }

        final boolean selectable = query.getType() == QueryType.D1 && !query.getOperationNames().isEmpty();

        try
        {
            TableModel tableModel = tableModelService.getTableModel(query, parameters);

            switch (requestSubUrl)
            {
                case "":
                    JsonApiModel document = documentGenerator.getJsonApiModel(query, parameters, tableModel);
                    document.setMeta(responseHelper.getDefaultMeta(req));
                    res.sendAsJson(document);
                    return;
                case "update":
                    res.sendAsJson(new MoreRows(
                            tableModel.getTotalNumberOfRows().intValue(),
                            tableModel.getTotalNumberOfRows().intValue(),
                            new MoreRowsBuilder(selectable).build(tableModel)
                    ));
                    return;
                default:
                    responseHelper.sendUnknownActionError();
            }
        }
        catch (Be5Exception e)
        {
            sendError(req, res, url, e);
        }
        catch (Throwable e)
        {
            sendError(req, res, url, Be5Exception.internalInQuery(e, query));
        }
    }

    private void sendError(Request req, Response res, HashUrl url, Be5Exception e)
    {
        log.log(Level.SEVERE, "Error in table" + url.toString(), e);

        String message = "";

        //message += GroovyRegister.getErrorCodeLine(e, query.getQuery());

        responseHelper.sendErrorAsJson(
                responseHelper.getErrorModel(e, message, Collections.singletonMap(SELF_LINK, url.toString())),
                responseHelper.getDefaultMeta(req)
        );
    }

}
