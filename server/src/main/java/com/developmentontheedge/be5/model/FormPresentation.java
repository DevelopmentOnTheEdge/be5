package com.developmentontheedge.be5.model;

import javax.json.JsonObject;
import java.util.Map;

public class FormPresentation
{

    public final String entity;
    public final String query;
    public final String operation;
    public final String title;
    public final String selectedRows;
    public final JsonObject bean;
    public final Object layout;
    public final Map<String, Object> parameters;

    public FormPresentation(String entity, String query, String operation, String title, String selectedRows, JsonObject bean, Object layout, Map<String, Object> parameters)
    {
        this.entity = entity;
        this.query = query;
        this.operation = operation;
        this.title = title;
        this.selectedRows = selectedRows;
        this.bean = bean;
        this.layout = layout;
        this.parameters = parameters;
    }

    public String getEntity()
    {
        return entity;
    }

    public String getQuery()
    {
        return query;
    }

    public String getOperation()
    {
        return operation;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSelectedRows()
    {
        return selectedRows;
    }

    public JsonObject getBean()
    {
        return bean;
    }

    public Object getLayout()
    {
        return layout;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
