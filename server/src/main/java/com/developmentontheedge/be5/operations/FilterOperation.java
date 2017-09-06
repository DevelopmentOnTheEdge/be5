package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.components.FrontendConstants;
import com.developmentontheedge.be5.metadata.util.Strings2;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.HashMap;
import java.util.Map;

public class FilterOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = dpsHelper.getDpsWithoutAutoIncrement(getInfo().getEntity());

        //getInfo().getModel()
        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        Map<String, String> params = new HashMap<>();

        for (DynamicProperty property : dps){
            if(property.getValue() != null && !Strings2.isNullOrEmpty(property.getValue().toString()))//todo utils?
            {
                params.put("+F" + property.getName(), property.getValue().toString());
            }
        }

        setResult(OperationResult.redirect(
                new HashUrl(FrontendConstants.TABLE_ACTION, getInfo().getEntity().getName(), context.queryName)
                        .named(params)
        ));
    }
}
