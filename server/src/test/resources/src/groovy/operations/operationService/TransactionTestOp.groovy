package src.groovy.operations.operationService

import com.developmentontheedge.be5.operation.support.GOperationSupport
import com.developmentontheedge.be5.operation.TransactionalOperation


class TransactionTestOp extends GOperationSupport implements TransactionalOperation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        database.testtableAdmin << [ name: "test", value: 1 ]

        throw new RuntimeException("test")
    }

}
