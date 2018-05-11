package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.model.FrontendAction;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;


public interface FrontendActions
{
    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
    String GO_BACK = "GO_BACK";
    String OPEN_NEW_WINDOW = "OPEN_NEW_WINDOW";

    //String UPDATE_DOCUMENT = "UPDATE_DOCUMENT";

    FrontendAction GO_BACK_ACTION = new FrontendAction(GO_BACK, null);

    static FrontendAction updateParentDocument(JsonApiModel model)
    {
        return new FrontendAction(UPDATE_PARENT_DOCUMENT, model);
    }

    static FrontendAction goBack()
    {
        return GO_BACK_ACTION;
    }

    static FrontendAction openNewWindow(String url)
    {
        return new FrontendAction(OPEN_NEW_WINDOW, url);
    }
}
