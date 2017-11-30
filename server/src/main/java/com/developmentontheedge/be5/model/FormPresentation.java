package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;

import javax.json.JsonObject;

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
    public final OperationResult operationResult;
    public final ErrorModel errorModel;

    public FormPresentation(OperationInfo operationInfo, String title, String selectedRows,
                            JsonObject bean, Object layout, OperationResult operationResult, ErrorModel errorModel)
    {
        this.entity = operationInfo.getEntityName();
        this.query = operationInfo.getQueryName();
        this.operation = operationInfo.getName();
        this.title = title;
        this.selectedRows = selectedRows;
        this.bean = bean;
        this.layout = layout;
        this.operationResult = operationResult;
        this.errorModel = errorModel;
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

    public OperationResult getOperationResult()
    {
        return operationResult;
    }

    public ErrorModel getErrorModel()
    {
        return errorModel;
    }
}
