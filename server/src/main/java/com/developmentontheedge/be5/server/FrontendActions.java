package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;


public interface FrontendActions
{
    String CLOSE_MAIN_MODAL = "CLOSE_MAIN_MODAL";

    String UPDATE_DOCUMENT = "UPDATE_DOCUMENT";
    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String OPEN_NEW_WINDOW = "OPEN_NEW_WINDOW";
    String REDIRECT = "REDIRECT";

    FrontendAction CLOSE_MAIN_MODAL_ACTION = new FrontendAction(CLOSE_MAIN_MODAL, null);
    FrontendAction GO_BACK_ACTION = new FrontendAction(GO_BACK, null);

    static FrontendAction updateDocument(JsonApiModel model)
    {
        return new FrontendAction(UPDATE_DOCUMENT, model);
    }

    static FrontendAction updateParentDocument(JsonApiModel model)
    {
        return new FrontendAction(UPDATE_PARENT_DOCUMENT, model);
    }

    static FrontendAction closeMainModal()
    {
        return CLOSE_MAIN_MODAL_ACTION;
    }

    static FrontendAction goBack()
    {
        return GO_BACK_ACTION;
    }

    static FrontendAction openNewWindow(String url)
    {
        return new FrontendAction(OPEN_NEW_WINDOW, url);
    }

    static FrontendAction redirect(String url)
    {
        return new FrontendAction(REDIRECT, url);
    }
}
