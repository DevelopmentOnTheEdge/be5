package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.services.document.DocumentGenerator;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.CONTEXT_PARAMS;
import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;
import static java.util.Objects.requireNonNull;

@Singleton
public class DocumentController extends JsonApiModelController
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private final DocumentGenerator documentGenerator;
    private final ErrorModelHelper errorModelHelper;

    @Inject
    public DocumentController(DocumentGenerator documentGenerator, ErrorModelHelper errorModelHelper)
    {
        this.documentGenerator = documentGenerator;
        this.errorModelHelper = errorModelHelper;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String requestSubUrl)
    {
        requireNonNull(req.get(TIMESTAMP_PARAM));
        String entityName = req.getNonEmpty(ENTITY_NAME_PARAM);
        String queryName = req.getNonEmpty(QUERY_NAME_PARAM);
        Map<String, Object> parameters = ParseRequestUtils.getContextParams(req.get(CONTEXT_PARAMS));
        try
        {
            switch (requestSubUrl)
            {
                case "":
                    return documentGenerator.getDocument(entityName, queryName, parameters);
                case "update":
                    return documentGenerator.getNewTableRows(entityName, queryName, parameters);
                case "json":
                    return documentGenerator.getTableRowsAsJson(entityName, queryName, parameters);
                default:
                    return null;
            }
        }
        catch (Be5Exception e)
        {
            String url = new HashUrl(TABLE_ACTION, entityName, queryName)
                    .named(parameters).toString();
            log.log(e.getLogLevel(), "Error in document: " + url, e);
            return error(errorModelHelper.getErrorModel(e, Collections.singletonMap(SELF_LINK, url)));
        }
    }
}
