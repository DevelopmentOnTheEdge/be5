package testtableAdmin

import com.developmentontheedge.be5.groovy.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.support.BaseOperationSupport

class CustomOperation extends BaseOperationSupport implements Operation
{
    GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport()

    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name", "value"], context.getParams(), values)
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
