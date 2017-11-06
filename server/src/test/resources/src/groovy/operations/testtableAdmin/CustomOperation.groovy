package testtableAdmin

import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext


class CustomOperation extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = new GDynamicPropertySetSupport(dpsHelper.getDpsForColumns(getInfo().getEntity(), ["name", "value"], presetValues), this)

        def newCalculatedValue = '4'

        dps.edit("value") {
            value = newCalculatedValue
            READ_ONLY = true
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }

}
