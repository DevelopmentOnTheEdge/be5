package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.server.helpers.FilterHelper
import javax.inject.Inject
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.model.TransactionalOperation
import com.developmentontheedge.beans.DynamicPropertySet


class OldRedirectFilter extends GOperationSupport implements TransactionalOperation
{
    @Inject private FilterHelper filterHelper

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), context.getOperationParams())

        return filterHelper.processFilterParams(dps, presetValues, context.getOperationParams())
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        redirectToTable(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters))
    }

}