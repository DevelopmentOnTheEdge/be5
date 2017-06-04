package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

public interface OperationService
{
    Either<FormPresentation, OperationResult> generate(Request req);

    OperationResult execute(Request req);
}
