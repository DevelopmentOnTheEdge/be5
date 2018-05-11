package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.model.FrontendAction;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;


public interface FrontendConstants
{
    /**
     * The first part of the link to the table.
     */
    String TABLE_ACTION = "table";

    String STATIC_ACTION = "static";
    
    /**
     * The first part of the link to the form.
     */
    String FORM_ACTION = "form";

    String OPERATION_RESULT = "operationResult";

    String CATEGORY_ID_PARAM = "_cat_";
    String SEARCH_PARAM = "_search_";
    String SEARCH_PRESETS_PARAM = "_search_presets_";

    String RELOAD_CONTROL_NAME = "_reloadcontrol_";

    String TOP_FORM = "topForm";
    //String TOP_DOCUMENT = "topDocument";

    String UPDATE_PARENT_DOCUMENT = "UPDATE_PARENT_DOCUMENT";
//    String GO_BACK = "GO_BACK";
//    String GO_BACK_AND_UPDATE = "GO_BACK_AND_UPDATE";

    static FrontendAction updateParentDocument(JsonApiModel model)
    {
        return new FrontendAction(UPDATE_PARENT_DOCUMENT, model);
    }
}
