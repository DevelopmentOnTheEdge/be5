package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

public interface OperationService
{
    void initOperationMap();

    Either<FormPresentation, OperationResult> generate(Request req);

    Either<FormPresentation, OperationResult> execute(Request req);

//    Operation create(Operation operation);
//
//    Operation create(OperationInfo meta);
}
