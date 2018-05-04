package com.developmentontheedge.be5.api;


import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;

import java.util.Collections;
import java.util.Map;

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

    static Map<String, Object> updateParentDocument(JsonApiModel model)
    {
        return Collections.singletonMap(UPDATE_PARENT_DOCUMENT, model);
    }
}
