package src.groovy.operations.testtableAdmin

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationSupport


class CustomOperation extends OperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.getDpsForColumns(getInfo().getEntity(), ["name", "value"], presetValues)

        def newCalculatedValue = '4'

        dps["value"] << [value: newCalculatedValue, READ_ONLY: true]

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {

    }

}
