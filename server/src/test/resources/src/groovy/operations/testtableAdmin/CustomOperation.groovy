package testtableAdmin

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.server.operations.support.GOperationSupport

class CustomOperation extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(params, getInfo().getEntity(), ["name", "value"], context.getParams(), presetValues)

        def newCalculatedValue = '4'

        params.edit("value") {
            value = newCalculatedValue
            READ_ONLY = true
        }

        return params
    }

    @Override
    void invoke(Object parameters) throws Exception
    {

    }

}
