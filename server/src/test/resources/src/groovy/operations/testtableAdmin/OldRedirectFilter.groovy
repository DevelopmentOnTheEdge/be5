package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.api.helpers.FilterHelper
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.TransactionalOperation
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
        setResult(OperationResult.redirectToTable(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters)))
    }

}