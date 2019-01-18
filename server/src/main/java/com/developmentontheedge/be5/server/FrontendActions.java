package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FormRequest;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;
import java.util.Objects;

import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION_PARAMS;


public interface FrontendActions
{
    String UPDATE_USER_INFO = "UPDATE_USER_INFO";
    String OPEN_DEFAULT_ROUTE = "OPEN_DEFAULT_ROUTE";
    FrontendAction OPEN_DEFAULT_ROUTE_ACTION = new FrontendAction(OPEN_DEFAULT_ROUTE, null);

    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String SET_URL = "SET_URL";
    String REDIRECT = "REDIRECT";
    String DOWNLOAD_OPERATION = "DOWNLOAD_OPERATION";
    String REFRESH_DOCUMENT = "REFRESH_DOCUMENT";

    FrontendAction GO_BACK_ACTION = new FrontendAction(GO_BACK, null);
    FrontendAction REFRESH_DOCUMENT_ACTION = new FrontendAction(REFRESH_DOCUMENT, null);

    static FrontendAction updateUserInfo(UserInfoModel userInfoModel)
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
                                            Map<String, Object> contextParams, Object parameters)
    {
        Map<String, Object> map = ((DynamicPropertySet) parameters).asModifiableMap();
        FormRequest formRequest = new FormRequest(entityName, queryName, operationName, contextParams);
        map.put(OPERATION_PARAMS, formRequest);
        return new FrontendAction(DOWNLOAD_OPERATION, map);
    }
}
