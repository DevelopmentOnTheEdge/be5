package src.groovy.operations.operationService

import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.TransactionalOperation


class TransactionTestOp extends GOperationSupport implements TransactionalOperation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        database.testtableAdmin << [ name: "test", value: 1 ]

        throw new RuntimeException("test")
    }

}
