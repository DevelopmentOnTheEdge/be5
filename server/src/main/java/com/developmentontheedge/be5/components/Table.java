package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.query.DocumentGenerator;
import com.developmentontheedge.be5.query.impl.MoreRows;
import com.developmentontheedge.be5.query.impl.MoreRowsBuilder;
import com.developmentontheedge.be5.query.impl.model.TableModel;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.api.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class Table implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        DocumentGenerator documentGenerator = injector.get(DocumentGenerator.class);
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);

        Map<String, String> parameters = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);

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
            TableModel tableModel = documentGenerator.getTableModel(query, parameters);

            switch (req.getRequestUri())
            {
                case "":
                    JsonApiModel document = documentGenerator.getJsonApiModel(query, parameters, tableModel);
                    document.setMeta(req.getDefaultMeta());
                    res.sendAsJson(document);
                    return;
                case "update":
                    Long totalNumberOfRows = tableModel.getTotalNumberOfRows();
                    if( totalNumberOfRows == null )
                        totalNumberOfRows = TableModel.from(query, parameters, injector).count();

                    res.sendAsRawJson(new MoreRows(
                            totalNumberOfRows.intValue(),
                            totalNumberOfRows.intValue(),
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
        String message = "";

        //message += GroovyRegister.getErrorCodeLine(e, query.getQuery());

        res.sendErrorAsJson(
                new ErrorModel(e, message, Collections.singletonMap(SELF_LINK, url.toString())),
                req.getDefaultMeta()
        );
    }

}
