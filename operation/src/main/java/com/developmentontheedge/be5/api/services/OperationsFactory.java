package com.developmentontheedge.be5.api.services;


public interface OperationsFactory
{
    OperationBuilder get(String entityName, String operationName);
}
