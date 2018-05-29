package com.developmentontheedge.be5.operations.system;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operations.support.GOperationSupport;
import com.developmentontheedge.beans.DynamicPropertyBuilder;

import java.util.Map;

import static com.developmentontheedge.beans.BeanInfoConstants.LABEL_FIELD;


public class SessionVariablesEdit extends GOperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        Object variable = session.getAt(context.getRecord());
        if (variable == null)
        {
            setResult(OperationResult.error("Session variable \'" + String.valueOf(getContext().getRecords()[0]) + "\' not found"));
            return null;
        }

        dps.add(new DynamicPropertyBuilder("label", String.class)
                .attr(LABEL_FIELD, true)
                .value("Тип: " + variable.getClass().getName())
                .get());

        dps.add(new DynamicPropertyBuilder("newValue", variable.getClass())
                .title("Новое значение:")
                .value(presetValues.getOrDefault("newValue", variable))
                .get());

        return dps;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        session.putAt(context.getRecord(), dps.getValue("newValue"));
    }

}
