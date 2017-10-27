package src.groovy.operations

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.api.services.DatabaseService
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport
import com.developmentontheedge.be5.operation.TransactionalOperation


class TestTransactionalOperation extends OperationSupport implements TransactionalOperation
{
    @Inject private DatabaseService databaseService

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if(presetValues.get("nullValues") != null)
        {
            return null
        }
        else
        {
            if(databaseService.getCurrentTxConn() == null)throw Be5Exception.internal("not in transaction")

            return dpsHelper.getDpsExcludeAutoIncrement(getInfo().getEntity(), presetValues)
        }
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        if(databaseService.getCurrentTxConn() == null)throw Be5Exception.internal("not in transaction")
    }

}
