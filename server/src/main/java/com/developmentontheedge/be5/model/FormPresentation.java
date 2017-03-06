package com.developmentontheedge.be5.model;

import java.util.List;
import java.util.Map;

public class FormPresentation
{

    public final String entity;
    public final String query;
    public final String operation;
    public final String title;
    public final String selectedRows;
    public final List<Field> fields;
    public final Map<String, String> parameters;
    
    /**
     * Nullable.
     */
    public final String customAction;
    
    public FormPresentation(
            String entity,
            String query,
            String operation,
            String title,
            String selectedRows,
            List<Field> fields,
            Map<String, String> parameters,
            String customAction)
    {
        this.entity = entity;
        this.query = query;
        this.operation = operation;
        this.title = title;
        this.selectedRows = selectedRows;
        this.fields = fields;
        this.parameters = parameters;
        this.customAction = customAction;
    }
    
}
