package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class FilterByParamsInQueryOperation extends FilterOperation
{
    @Override
    public DynamicPropertySet getFilterParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addParamsFromQuery(dps, getInfo().getModel(), getQuery(), context.getParams());
        return dps;
    }
}
