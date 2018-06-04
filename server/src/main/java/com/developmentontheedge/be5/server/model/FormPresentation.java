package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.operation.model.OperationContext;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class FormPresentation
{
    private final String entity;
    private final String query;
    private final String operation;
    private final String title;//todo change to 'Здания - Добавить'
    private final String selectedRows;
    private final Map<String, Object> operationParams;
    private final JsonObject bean;
    private final Object layout;
    private final OperationResult operationResult;//todo remove
    private final ErrorModel errorModel;

    public FormPresentation(OperationInfo operationInfo, OperationContext context, String title,
                            JsonObject bean, Object layout, OperationResult operationResult, ErrorModel errorModel)
    {
        this.entity = operationInfo.getEntityName();
        this.query = context.getQueryName();
        this.operation = operationInfo.getName();
        this.title = title;
        this.selectedRows = Arrays.stream(context.getRecords()).map(Object::toString).collect(Collectors.joining(","));
        this.operationParams = context.getOperationParams();
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

    public Map<String, Object> getOperationParams()
    {
        return operationParams;
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
