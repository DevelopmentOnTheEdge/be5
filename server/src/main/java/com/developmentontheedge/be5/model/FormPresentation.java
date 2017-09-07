package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.operation.OperationResult;

import javax.json.JsonObject;
import java.util.Map;

public class FormPresentation
{
    public final String entity;
    public final String query;
    public final String operation;
    public final String title;//todo change to 'Здания - Добавить'
    // add public final String operationName;
    public final String selectedRows;
    public final JsonObject bean;
    public final Object layout;
    public final Map<String, Object> parameters;
    public final OperationResult operationResult;

    public FormPresentation(String entity, String query, String operation, String title, String selectedRows,
                            JsonObject bean, Object layout, Map<String, Object> parameters, OperationResult operationResult)
    {
        this.entity = entity;
        this.query = query;
        this.operation = operation;
        this.title = title;
        this.selectedRows = selectedRows;
        this.bean = bean;
        this.layout = layout;
        this.parameters = parameters;
        this.operationResult = operationResult;
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

    public OperationResult getOperationResult() {
        return operationResult;
    }
}
