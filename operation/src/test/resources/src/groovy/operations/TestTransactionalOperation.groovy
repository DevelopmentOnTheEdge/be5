package src.groovy.operations

import com.developmentontheedge.be5.exceptions.Be5Exception
import com.developmentontheedge.be5.database.ConnectionService
import com.developmentontheedge.be5.operation.TransactionalOperation
import com.developmentontheedge.be5.operation.support.TestOperationSupport
import com.developmentontheedge.beans.DynamicPropertySetSupport

import javax.inject.Inject

class TestTransactionalOperation extends TestOperationSupport implements TransactionalOperation
{
    @Inject
    private ConnectionService connectionService

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if (presetValues.get("nullValues") != null) {
            return null
        } else {
            if (!connectionService.isInTransaction()) throw Be5Exception.internal("not in inTransaction")

            return new DynamicPropertySetSupport()
        }
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        if (!connectionService.isInTransaction()) throw Be5Exception.internal("not in inTransaction")
    }

}
