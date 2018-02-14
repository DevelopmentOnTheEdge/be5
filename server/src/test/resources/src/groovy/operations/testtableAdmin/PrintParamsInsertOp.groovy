package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operations.SilentInsertOperation
import com.developmentontheedge.beans.DynamicPropertySet

class PrintParamsInsertOp extends SilentInsertOperation
{
    @Override
    void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(((DynamicPropertySet)parameters).asMap().toString()))
    }
}
