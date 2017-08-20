package com.developmentontheedge.be5.components;

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
    
    /**
     * Selected rows of a table.
     */
    String SELECTED_ROWS = "selectedRows";
    
    // MORE ROWS GENERATOR
    
    String START = "start";
    String LENGTH = "length";
    String DRAW = "draw";
    String SELECTABLE = "selectable";
    String TOTAL_NUMBER_OF_ROWS = "totalNumberOfRows";

    String TIMESTAMP_PARAM = "_ts_";

    String SELF_LINK = "self";
}
