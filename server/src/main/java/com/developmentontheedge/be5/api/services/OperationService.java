package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

import java.util.Map;


public interface OperationService
{
    Either<FormPresentation, OperationResult> generate(Request req);

    Either<FormPresentation, OperationResult> execute(Request req);

    Either<FormPresentation, OperationResult> generate(OperationInfo meta,
                Map<String, Object> presetValues, String selectedRowsString, Request req);

    Either<FormPresentation, OperationResult> execute(OperationInfo meta,
                Map<String, Object> presetValues, String selectedRowsString, Request req);

//    Operation create(Operation operation);
//
//    Operation create(OperationInfo meta);
}
