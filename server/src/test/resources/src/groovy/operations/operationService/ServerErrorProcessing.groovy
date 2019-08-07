package src.groovy.operations.operationService

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.server.operations.support.GOperationSupport
import groovy.transform.TypeChecked

@TypeChecked
class ServerErrorProcessing extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        dpsHelper.addDpForColumns(params, getInfo().getEntity(), ["name"], context.getParams(), presetValues)

        def name = params.getProperty("name")

        if (name.getValue() == "generateErrorInProperty") {
            validator.setError(name, "Error in property (getParameters)")
        }

        if (name.getValue() == "generateErrorWithTimeout") {
                setResult( OperationResult.error( "Error in property (getParameters) with timeout 20", null, 20 ) )
        }

        return params
    }

    @Override
    void invoke(Object parameters) throws Exception
    {

    }

}
