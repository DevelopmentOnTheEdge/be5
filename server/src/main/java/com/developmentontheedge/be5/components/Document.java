package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.components.impl.MoreRowsGenerator;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.components.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;


public class Document implements Component 
{
    private static final Logger log = Logger.getLogger(Document.class.getName());

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        DocumentGenerator documentGenerator = injector.get(DocumentGenerator.class);
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        int sortColumn = req.getInt("order[0][column]", -1);
        boolean sortDesc = "desc".equals(req.get("order[0][dir]"));

        Map<String, String> parametersMap = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);

        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);
        Query query = userAwareMeta.getQuery(entityName, queryName);

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    sendQueryResponseData(req, res, query,
                            documentGenerator.routeAndRun(query, parametersMap, sortColumn, sortDesc));
                    return;
                case "moreRows":
                    res.sendAsRawJson(new MoreRowsGenerator(injector).generate(req));
                    return;
                default:
                    res.sendUnknownActionError();
            }
        }
        catch (Be5Exception e)
        {
            sendQueryResponseError(req, res, query, e);
        }
        catch (Throwable e)
        {
            sendQueryResponseError(req, res, query, Be5Exception.internalInQuery(e, query));
        }
    }

    public static void sendQueryResponseData(Request req, Response res, Query query, Object data)
    {
        res.sendAsJson(
                new ResourceData(TABLE_ACTION, data),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, ActionHelper.toAction(query).arg)
        );
    }

    private void sendQueryResponseError(Request req, Response res, Query query, Be5Exception e)
    {
        String message = Be5Exception.getMessage(e);

        //message += GroovyRegister.getErrorCodeLine(e, query.getQuery());

        res.sendErrorAsJson(
                new ErrorModel("500", e.getTitle(), message, Be5Exception.exceptionAsString(e)),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, ActionHelper.toAction(query).arg)
        );
    }

}
