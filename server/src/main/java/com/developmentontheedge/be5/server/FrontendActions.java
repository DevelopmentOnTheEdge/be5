package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY;
import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION;
import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION_PARAMS;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY;
import static com.developmentontheedge.be5.server.RestApiConstants.VALUES;


public interface FrontendActions
{
    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String SET_URL = "SET_URL";
    String REDIRECT = "REDIRECT";
    String DOWNLOAD_OPERATION = "DOWNLOAD_OPERATION";

    FrontendAction GO_BACK_ACTION = new FrontendAction(GO_BACK, null);

    static FrontendAction setUrl(String url)
    {
        Objects.requireNonNull(url);
        return new FrontendAction(SET_URL, url);
    }

    static FrontendAction updateParentDocument(JsonApiModel model)
    {
        Objects.requireNonNull(model);
        return new FrontendAction(UPDATE_PARENT_DOCUMENT, model);
    }

    static FrontendAction goBack()
    {
        return GO_BACK_ACTION;
    }

    static FrontendAction goBackOrRedirect(String url)
    {
        return new FrontendAction(GO_BACK, url);
    }

    static FrontendAction redirect(String url)
    {
        Objects.requireNonNull(url);
        return new FrontendAction(REDIRECT, url);
    }

    static FrontendAction downloadOperation(String entityName, String queryName, String operationName,
                                            Map<String, Object> operationParams, Object parameters)
    {
        HashMap<String, Object> params = new HashMap<String, Object>()
        {
            {
                put(ENTITY, entityName);
                put(QUERY, queryName);
                put(OPERATION, operationName);
                put(OPERATION_PARAMS, operationParams);
            }
        };
        if (parameters != null)params.put(VALUES, JsonFactory.beanValues(parameters));
        return new FrontendAction(DOWNLOAD_OPERATION, params);
    }
}
