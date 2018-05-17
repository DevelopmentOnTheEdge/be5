package com.developmentontheedge.be5.api.services.databasemodel;

import com.developmentontheedge.be5.api.services.databasemodel.impl.OperationModelBase;
import com.developmentontheedge.be5.operation.Operation;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import java.util.Map;


public interface OperationModel 
{
    OperationModel setEntityName(String entityName);

    OperationModel setQueryName(String queryName);

    OperationModel setOperationName(String operationName);

    OperationModel setRecords(Object[] records);

    OperationModel setPresetValues(Map<String, ?> presetValues);

    OperationModel setOperationParams(Map<String, Object> operationParams);

    Object generate();

    Operation execute();

    Object generate(@DelegatesTo(OperationModelBase.GOperationModelBaseBuilder.class) Closure closure);

    Operation execute(@DelegatesTo(OperationModelBase.GOperationModelBaseBuilder.class) Closure closure);

    default Operation executeIfNotEmptyRecords(Object[] records)
    {
        if(records.length > 0)
        {
            setRecords(records);
            return execute();
        }
        else
        {
            return null;
        }
    }
}
