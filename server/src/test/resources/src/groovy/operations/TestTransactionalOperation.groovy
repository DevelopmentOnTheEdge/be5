package src.groovy.operations

import com.developmentontheedge.be5.api.exceptions.Be5Exception
import com.developmentontheedge.be5.api.services.DatabaseService
import com.developmentontheedge.be5.inject.Inject
import com.developmentontheedge.be5.operation.OperationSupport
import com.developmentontheedge.be5.operation.TransactionalOperation
import com.developmentontheedge.beans.DynamicPropertySetSupport


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
            if(databaseService.getCurrentTxConn() == null)throw Be5Exception.internal("not in transactionWithResult")

            return dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(), getInfo().getEntity(),
                    context.getOperationParams(), presetValues)
        }
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        if(databaseService.getCurrentTxConn() == null)throw Be5Exception.internal("not in transactionWithResult")
    }

}
