package testtableAdmin

import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext


class CustomOperation extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name", "value"], context.getOperationParams(), presetValues)

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
