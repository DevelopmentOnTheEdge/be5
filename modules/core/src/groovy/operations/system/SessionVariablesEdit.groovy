package system

import com.developmentontheedge.be5.operation.GOperationSupport
import com.developmentontheedge.be5.operation.Operation
import com.developmentontheedge.be5.operation.OperationContext


class SessionVariablesEdit extends GOperationSupport implements Operation
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        def variable = session[records[0]]
        if(variable != null)
        {
            dps << [
                    name        : "label",
                    value       : "Тип: " + variable.getClass().getName(),
                    LABEL_FIELD : true
            ]

            dps << [
                    name        : "newValue",
                    TYPE        : variable.getClass(),
                    DISPLAY_NAME: "Новое значение:",
                    value       : presetValues.getOrDefault("newValue", variable)
            ]
        }
        else
        {
            dpsHelper.addDpWithLabelANDNotSubmitted(dps, "Session variable '${records[0]}' not found")
        }

        return dps
    }

    @Override
    void invoke(Object parameters, OperationContext context) throws Exception
    {
        session[records[0]] = dps.$newValue
    }
}
