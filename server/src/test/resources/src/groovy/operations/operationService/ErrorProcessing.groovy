package src.groovy.operations.operationService

import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext
import com.developmentontheedge.be5.operation.OperationResult
import groovy.transform.TypeChecked


@TypeChecked
class ErrorProcessing extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name"], presetValues)

        dps.add("propertyForAnotherEntity") {
            value = "text"
        }

        if(presetValues.containsKey("booleanProperty"))
        {
            dps.add("booleanProperty") {
                TYPE  = Boolean
                value = presetValues.getOrDefault("booleanProperty", false)
            }
        }
        
        def name = dps.getProperty("name")

        if(name.getValue() == "generateErrorInProperty")
        {
            validator.setError(name, "Error in property (getParameters)")
        }

        if(name.getValue() == "generateErrorStatus")
        {
            setResult(OperationResult.error("The operation can not be performed."))
        }

        if(name.getValue() == "generateError")
        {
            throw new IllegalArgumentException()
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        dps.remove("propertyForAnotherEntity")

        def name = dps.getProperty("name")

        if(name.getValue() == "executeErrorInProperty")
        {
            validator.setError(name, "Error in property (invoke)")
            return
        }

        if(name.getValue() == "executeErrorStatus")
        {
            setResult(OperationResult.error("An error occurred while performing operations."))
            return
        }

        if(name.getValue() == "executeError")
        {
            throw new IllegalArgumentException()
        }

        setResult(OperationResult.finished())
    }

}
