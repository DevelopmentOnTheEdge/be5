package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.developmentontheedge.be5.server.RestApiConstants.CONTEXT_PARAMS;
import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY_NAME_PARAM;


public interface FrontendActions
{
    String UPDATE_USER_INFO = "UPDATE_USER_INFO";
    String OPEN_DEFAULT_ROUTE = "OPEN_DEFAULT_ROUTE";
    FrontendAction OPEN_DEFAULT_ROUTE_ACTION = new FrontendAction(OPEN_DEFAULT_ROUTE, null);

    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String CLOSE_MAIN_MODAL = "CLOSE_MAIN_MODAL";
    String SET_URL = "SET_URL";
    String REDIRECT = "REDIRECT";
    String DOWNLOAD_OPERATION = "DOWNLOAD_OPERATION";
    String REFRESH_DOCUMENT = "REFRESH_DOCUMENT";

    FrontendAction GO_BACK_ACTION = new FrontendAction(GO_BACK, null);
    FrontendAction CLOSE_MAIN_MODAL_ACTION = new FrontendAction(CLOSE_MAIN_MODAL, null);
    FrontendAction REFRESH_DOCUMENT_ACTION = new FrontendAction(REFRESH_DOCUMENT, null);

    static FrontendAction updateUser(UserInfoModel userInfoModel)
    {
        return new FrontendAction(UPDATE_USER_INFO, userInfoModel);
    }

    static FrontendAction[] updateUserAndOpenDefaultRoute(UserInfoModel userInfoModel)
    {
        return new FrontendAction[]{
                new FrontendAction(UPDATE_USER_INFO, userInfoModel),
                OPEN_DEFAULT_ROUTE_ACTION
        };
    }

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

    static FrontendAction closeMainModal()
    {
        return CLOSE_MAIN_MODAL_ACTION;
    }

    static FrontendAction goBackOrRedirect(String url)
    {
        return new FrontendAction(GO_BACK, url);
    }

    static FrontendAction refreshDocument()
    {
        return REFRESH_DOCUMENT_ACTION;
    }

    static FrontendAction redirect(String url)
    {
        Objects.requireNonNull(url);
        return new FrontendAction(REDIRECT, url);
    }

    static FrontendAction downloadOperation(String entityName, String queryName, String operationName,
                                            Map<String, Object> contextParams, Map<String, Object> parameters)
    {
        Map<Object, Object> map = new HashMap<>(parameters);
        map.put(ENTITY_NAME_PARAM, entityName);
        map.put(QUERY_NAME_PARAM, queryName);
        map.put(OPERATION_NAME_PARAM, operationName);
        map.put(CONTEXT_PARAMS, contextParams);
        return new FrontendAction(DOWNLOAD_OPERATION, map);
    }
}
