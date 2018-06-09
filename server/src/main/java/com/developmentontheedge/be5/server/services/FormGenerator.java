package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.Map;


public interface FormGenerator
{
    JsonApiModel getJsonApiModel(String method, String entityName, String queryName, String operationName,
                                 String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values);

    Either<FormPresentation, OperationResult> generate(Operation operation, Map<String, ?> values);

    Either<FormPresentation, OperationResult> execute(Operation operation, Map<String, ?> values);

    ErrorModel getErrorModel(Throwable e, HashUrl url);
}
