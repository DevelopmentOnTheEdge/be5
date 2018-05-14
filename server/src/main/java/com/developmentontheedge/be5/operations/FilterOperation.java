package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.google.inject.Inject;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.support.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;

import static com.developmentontheedge.be5.api.FrontendActions.*;


public class FilterOperation extends OperationSupport
{
    @Inject private FilterHelper filterHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getModel(), context.getOperationParams());

        return filterHelper.processFilterParams(dps, presetValues, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        setResult(OperationResult.finished(null,
                updateParentDocument(filterHelper.filterDocument(getQuery(), parameters))));
    }
}
