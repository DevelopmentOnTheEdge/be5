package src.groovy.extenders

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operations.support.OperationExtenderSupport

class TestGroovyExtender extends OperationExtenderSupport
{
    @Override
    void preInvoke(Operation op, Object parameters) throws Exception
    {
        db.update("update testTable name = 'preInvokeBeforeSkipGroovy' WHERE 1=2")
    }
}
