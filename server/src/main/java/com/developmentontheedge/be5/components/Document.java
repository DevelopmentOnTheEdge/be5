package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.query.DocumentGenerator;
import com.developmentontheedge.be5.query.impl.MoreRowsGenerator;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.api.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class Document implements Component 
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        DocumentGenerator documentGenerator = injector.get(DocumentGenerator.class);
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        int sortColumn = req.getInt("order[0][column]", -1);
        boolean sortDesc = "desc".equals(req.get("order[0][dir]"));
        Map<String, String> parametersMap = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);

        HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parametersMap);

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

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    JsonApiModel document = documentGenerator.getDocument(query, parametersMap, sortColumn, sortDesc);
                    document.setMeta(req.getDefaultMeta());
                    res.sendAsJson(document);
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
