package com.developmentontheedge.be5.server;


public interface RestApiConstants
{
    /**
     * Page category.
     */
    String ENTITY = "entity";
    
    /**
     * Page name.
     */
    String QUERY = "query";
    
    /**
     * Action name.
     */
    String OPERATION = "operation";
    
    /**
     * A parameter that contains a JSON array of objects with the fields "name" and "value".
     */
    String VALUES = "values";

    String OPERATION_PARAMS = "operationParams";
    
    /**
     * Selected rows of a table.
     */
    String SELECTED_ROWS = "selectedRows";

    String OFFSET = "_offset_";
    String LIMIT = "_limit_";
    String ORDER_COLUMN = "_orderColumn_";
    String ORDER_DIR = "_orderDir_";

    // json api

    String TIMESTAMP_PARAM = "_ts_";

    String SELF_LINK = "self";
}
