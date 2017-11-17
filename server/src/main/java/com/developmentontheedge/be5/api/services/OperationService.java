package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

import java.util.Map;


public interface OperationService
{
    Either<FormPresentation, OperationResult> generate(Operation operation, Map<String, Object> presetValues);

    Either<FormPresentation, OperationResult> execute(Operation operation, Map<String, Object> presetValues);

    Either<FormPresentation, OperationResult> generate(Operation operation);

    Either<FormPresentation, OperationResult> execute(Operation operation);
}
