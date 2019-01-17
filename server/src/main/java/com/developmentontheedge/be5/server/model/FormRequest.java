package com.developmentontheedge.be5.server.model;

import java.util.Map;

public class FormRequest
{
    public String entity;
    public String query;
    public String operation;
    public Map<String, Object> contextParams;
    public Long _ts_;

    public FormRequest()
    {
    }

    public FormRequest(String entity, String query, String operation,
                       Map<String, Object> contextParams, Long _ts_)
    {
        this.entity = entity;
        this.query = query;
        this.operation = operation;
        this.contextParams = contextParams;
        this._ts_ = _ts_;
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

    public Long get_ts_()
    {
        return _ts_;
    }

    public void set_ts_(Long _ts_)
    {
        this._ts_ = _ts_;
    }
}
