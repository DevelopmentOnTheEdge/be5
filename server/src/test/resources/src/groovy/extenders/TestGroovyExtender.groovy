package src.groovy.extenders

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationExtenderSupport

class TestGroovyExtender extends OperationExtenderSupport
{
    @Override
    boolean skipInvoke(Operation op, Object parameters)
    {
        return true
    }
}
