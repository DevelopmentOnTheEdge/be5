package src.groovy.operations

import com.developmentontheedge.be5.base.exceptions.Be5Exception
import com.developmentontheedge.be5.database.ConnectionService
import com.developmentontheedge.be5.operation.model.TransactionalOperation
import com.developmentontheedge.be5.server.operations.support.OperationSupport
import com.developmentontheedge.beans.DynamicPropertySetSupport

import javax.inject.Inject

class TestTransactionalOperation extends OperationSupport implements TransactionalOperation
{
    @Inject
    private ConnectionService connectionService

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if (presetValues.get("nullValues") != null) {
            return null
        } else {
            if (connectionService.getCurrentTxConn() == null) throw Be5Exception.internal("not in transactionWithResult")

            return dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(), getInfo().getEntity(),
                    context.params, presetValues)
        }
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        if (connectionService.getCurrentTxConn() == null) throw Be5Exception.internal("not in transactionWithResult")
    }

}
