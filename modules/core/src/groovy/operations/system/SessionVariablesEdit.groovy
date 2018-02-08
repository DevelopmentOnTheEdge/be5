package system

import com.developmentontheedge.be5.operation.GOperationSupport


class SessionVariablesEdit extends GOperationSupport
{
    @Override
    Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        def variable = session[context.records[0]]
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
            dpsHelper.addDpWithLabelANDNotSubmitted(dps, "Session variable '${context.records[0]}' not found")
        }

        return dps
    }

    @Override
    void invoke(Object parameters) throws Exception
    {
        session[context.records[0]] = (Object)dps.$newValue
    }
}
