package src.groovy.operations.operationService

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

        return params
    }

    @Override
    void invoke(Object parameters) throws Exception
    {

    }

}
