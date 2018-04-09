package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


public class FilterByParamsInQueryOperation extends OperationSupport
{
    @Inject private FilterHelper filterHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addParamsFromQuery(dps, getInfo().getModel(), getQuery(), context.getOperationParams());

        return filterHelper.processFilterParams(dps, presetValues, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.document(filterHelper.filterDocument(getQuery(), parameters)));
    }
}
