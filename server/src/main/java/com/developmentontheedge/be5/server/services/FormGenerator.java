package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import java.util.Map;


public interface FormGenerator
{
//    JsonApiModel getJsonApiModel(String method, String entityName, String queryName, String operationName,
//                                 String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);

//    Either<FormPresentation, OperationResult> generate(Operation operation, Map<String, ?> values);
//
//    Either<FormPresentation, OperationResult> execute(Operation operation, Map<String, ?> values);

    ResourceData generate(String entityName, String queryName, String operationName,
                          String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);

    ResourceData execute(String entityName, String queryName, String operationName,
                         String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);

    //ErrorModel getErrorModel(Throwable e, HashUrl url);
}
