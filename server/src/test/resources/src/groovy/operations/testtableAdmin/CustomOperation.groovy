package testtableAdmin

import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.model.Operation


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
