package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import java.util.Map;


public interface FormGenerator
{
    ResourceData generate(String entityName, String queryName, String operationName,
                          String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);

    ResourceData execute(String entityName, String queryName, String operationName,
                         String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);
}
