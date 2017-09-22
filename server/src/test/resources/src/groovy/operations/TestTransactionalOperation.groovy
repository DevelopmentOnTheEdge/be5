package src.groovy.operations

import com.developmentontheedge.be5.api.helpers.UserAwareMeta
import com.developmentontheedge.be5.api.helpers.impl.UserAwareMetaImpl
import com.developmentontheedge.be5.api.services.DatabaseService
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport
import com.developmentontheedge.be5.operation.TransactionalOperation

import java.sql.Date
import java.text.SimpleDateFormat


class TestTransactionalOperation extends OperationSupport implements TransactionalOperation
{
    @Inject private DatabaseService databaseService

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //databaseService.isInTransaction()
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }

}
