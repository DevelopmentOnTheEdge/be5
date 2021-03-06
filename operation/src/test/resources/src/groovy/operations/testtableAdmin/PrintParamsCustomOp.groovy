package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.groovy.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.support.BaseOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet

class PrintParamsCustomOp extends BaseOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //return dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), context.getParams(), values)
        def dps = new GDynamicPropertySetSupport()
        dps.add("name") { value = presetValues.get("name")}
        dps.add("value") { value = presetValues.get("value")}

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(((DynamicPropertySet) parameters).asMap().toString()))
    }
}
