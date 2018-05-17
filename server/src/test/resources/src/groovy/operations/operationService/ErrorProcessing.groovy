package src.groovy.operations.operationService

import com.developmentontheedge.be5.operations.support.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationResult
import com.developmentontheedge.be5.operation.OperationStatus
import groovy.transform.TypeChecked

import static org.junit.Assert.assertEquals


@TypeChecked
class ErrorProcessing extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if(presetValues.get("name") == "withoutParams")
        {
            return null
        }

        dpsHelper.addDpForColumns(dps, getInfo().getEntity(), ["name"], context.getOperationParams(), presetValues)

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

        if(name.getValue() == "generateCall")
        {
            assertEquals(OperationStatus.GENERATE, getStatus())
        }

        if(name.getValue() == "executeErrorInProperty")
        {
            assertEquals(OperationStatus.EXECUTE, getStatus())
        }

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
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
