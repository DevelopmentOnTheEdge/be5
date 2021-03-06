package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;

import java.util.Map;


public interface OperationService
{
    Either<Object, OperationResult> generate(Operation operation, Map<String, Object> values);

    Either<Object, OperationResult> execute(Operation operation, Map<String, Object> values);

}
