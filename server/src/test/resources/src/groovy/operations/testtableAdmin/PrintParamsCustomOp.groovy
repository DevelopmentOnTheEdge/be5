package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet

class PrintParamsCustomOp extends GOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), context.getOperationParams(), presetValues)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(((DynamicPropertySet)parameters).asMap().toString()))
    }
}
