package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.base.model.GDynamicPropertySetSupport
import com.developmentontheedge.be5.base.util.DpsUtils
import com.developmentontheedge.be5.operation.model.OperationResult
import com.developmentontheedge.be5.operation.support.BaseOperationSupport
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport


class PrintParamsCustomOp extends BaseOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //return dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), context.getOperationParams(), presetValues)
        def dps = new GDynamicPropertySetSupport()
        dps.add("name")
        dps.add("value")

        return DpsUtils.setValues(dps, presetValues)
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(((DynamicPropertySet)parameters).asMap().toString()))
    }
}
