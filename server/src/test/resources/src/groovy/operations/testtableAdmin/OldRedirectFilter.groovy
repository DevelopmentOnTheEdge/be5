package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.operation.TransactionalOperation
import com.developmentontheedge.be5.server.services.FilterHelper
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet

import javax.inject.Inject

class OldRedirectFilter extends GOperationSupport implements TransactionalOperation
{
    @Inject
    private FilterHelper filterHelper

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpExcludeAutoIncrement(params, getInfo().getEntity(), context.getOperationParams())

        return filterHelper.processFilterParams(params, presetValues, context.getOperationParams())
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        redirectToTable(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters))
    }

}
