package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.be5.operation.OperationResult;

import java.util.Map;


public interface OperationModel 
{
    OperationModel setEntityName(String entityName);

    OperationModel setQueryName(String queryName);

    OperationModel setOperationName(String operationName);

    OperationModel setRecords(String... records);

    OperationModel setPresetValues(Map<String, Object> presetValues);

    Object getParameters() throws Exception;

    OperationResult execute();
}
