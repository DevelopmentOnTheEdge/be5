package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.server.services.FilterHelper;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Map;


public class FilterByParamsInQueryOperation extends FilterOperation
{
    @Inject
    private FilterHelper filterHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addParamsFromQuery(dps, getInfo().getModel(), getQuery(), context.getParams());

        return filterHelper.processFilterParams(dps, presetValues, context.getParams());
    }

}
