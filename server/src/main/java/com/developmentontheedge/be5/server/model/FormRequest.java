package com.developmentontheedge.be5.server.model;

import java.util.Map;

public class FormRequest
{
    public String entity;
    public String query;
    public String operation;
    public Map<String, Object> contextParams;

    public FormRequest()
    {
    }

    public FormRequest(String entity, String query, String operation,
                       Map<String, Object> contextParams)
    {
        this.entity = entity;
        this.query = query;
        this.operation = operation;
        this.contextParams = contextParams;
    }

    public String getEntity()
    {
        return entity;
    }

    public void setEntity(String entity)
    {
        this.entity = entity;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public Map<String, Object> getContextParams()
    {
        return contextParams;
    }

    public void setContextParams(Map<String, Object> contextParams)
    {
        this.contextParams = contextParams;
    }
}
