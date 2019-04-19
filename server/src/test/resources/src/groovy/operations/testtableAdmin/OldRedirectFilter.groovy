package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.operation.TransactionalOperation
import com.developmentontheedge.be5.server.operations.FilterOperation
import com.developmentontheedge.beans.DynamicPropertySet

class OldRedirectFilter extends FilterOperation implements TransactionalOperation
{
    @Override
    void invoke(Object parameters) throws Exception
    {
        redirectToTable(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters))
    }
}
