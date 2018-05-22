package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.query.model.MoreRows;
import com.developmentontheedge.be5.query.model.MoreRowsBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.util.HashUrl;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class TableController extends ControllerSupport
{
    private static final Logger log = Logger.getLogger(TableController.class.getName());

    private final DocumentGenerator documentGenerator;
    private final TableModelService tableModelService;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public TableController(DocumentGenerator documentGenerator, TableModelService tableModelService, UserAwareMeta userAwareMeta)
    {
        this.documentGenerator = documentGenerator;
        this.tableModelService = tableModelService;
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public void generate(Request req, Response res)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);

        Map<String, Object> parameters = req.getValuesFromJson(RestApiConstants.VALUES);

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

            switch (req.getRequestUri())
            {
                case "":
                    JsonApiModel document = documentGenerator.getJsonApiModel(query, parameters, tableModel);
                    document.setMeta(req.getDefaultMeta());
                    res.sendAsJson(document);
                    return;
                case "update":
                    res.sendAsRawJson(new MoreRows(
                            tableModel.getTotalNumberOfRows().intValue(),
                            tableModel.getTotalNumberOfRows().intValue(),
                            new MoreRowsBuilder(selectable).build(tableModel)
                    ));
                    return;
                default:
                    res.sendUnknownActionError();
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

        res.sendErrorAsJson(
                new ErrorModel(e, message, Collections.singletonMap(SELF_LINK, url.toString())),
                req.getDefaultMeta()
        );
    }

}