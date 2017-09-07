package src.groovy.operations.operationService

import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationSupport


class ErrorProcessing extends OperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dps = dpsHelper.getDpsForColumns(getInfo().getEntity(), ["name"], presetValues)

        def name = dps.getProperty("name")
        if(name.getValue() == "errorInProperty")
        {
            validator.setError(name, "Error in property")
        }

        if(name.getValue() == "generateErrorStatus")
        {
            setResult(OperationResult.error("The operation can not be performed."))
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        if(dps.getValue("name") == "invokeErrorStatus")
        {
            setResult(OperationResult.error("Error in invoke."))
        }
    }

}
