package com.developmentontheedge.be5.api.services;

//import com.developmentontheedge.be5.databasemodel.groovy.GOperationModelBaseBuilder;
import com.developmentontheedge.be5.operation.model.Operation;
//import groovy.lang.Closure;
//import groovy.lang.DelegatesTo;

import java.util.Map;


public interface OperationBuilder
{
    //OperationBuilder setEntityName(String entityName);

    OperationBuilder setQueryName(String queryName);

    //OperationBuilder setOperationName(String operationName);

    OperationBuilder setRecords(Object[] records);

    OperationBuilder setPresetValues(Map<String, ?> presetValues);

    OperationBuilder setOperationParams(Map<String, Object> operationParams);

    Object generate();

    Operation execute();

//    Object generate(@DelegatesTo(GOperationModelBaseBuilder.class) Closure closure);
//
//    Operation execute(@DelegatesTo(GOperationModelBaseBuilder.class) Closure closure);

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
