package src.groovy.extenders

import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.operation.support.BaseOperationExtenderSupport

class TestGroovyExtender extends BaseOperationExtenderSupport {
    @Override
    void preInvoke(Operation op, Object parameters) throws Exception {
        db.update("update testTable name = 'preInvokeBeforeSkipGroovy' WHERE 1=2")
    }
}
