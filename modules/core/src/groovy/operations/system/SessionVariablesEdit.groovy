package system

import com.developmentontheedge.be5.operation.support.GOperationSupport
import com.developmentontheedge.be5.operation.OperationResult


class SessionVariablesEdit extends GOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        def variable = session[context.records[0]]
        if(variable == null)
        {
            setResult(OperationResult.error("Session variable '${context.records[0]}' not found"))
            return null
        }

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

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        session[context.records[0]] = (Object)dps.$newValue
    }
}
