package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.Objects;


public interface FrontendActions
{
    String CLOSE_MAIN_MODAL = "CLOSE_MAIN_MODAL";
    String MAIN_DOCUMENT = "MAIN_DOCUMENT";
    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String SET_URL = "SET_URL";
    String REDIRECT = "REDIRECT";

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

    static FrontendAction redirect(String url)
    {
        Objects.requireNonNull(url);
        return new FrontendAction(REDIRECT, url);
    }
}
