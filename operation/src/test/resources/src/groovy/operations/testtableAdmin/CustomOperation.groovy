package testtableAdmin

import com.developmentontheedge.be5.base.model.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.model.Operation
import com.developmentontheedge.be5.operation.support.BaseOperationSupport

class CustomOperation extends BaseOperationSupport implements Operation
{
    GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport()

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name", "value"], context.getOperationParams(), values)
        dps.add("name")
        dps.add("value")

        def newCalculatedValue = '4'

        dps.edit("value") {
            value = newCalculatedValue
            READ_ONLY = true
        }

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {

    }

}
