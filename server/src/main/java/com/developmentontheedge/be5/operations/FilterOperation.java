package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.api.helpers.FilterHelper;
import com.developmentontheedge.be5.inject.Inject;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.Map;


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
        setResult(OperationResult.finished(null, FrontendConstants.
                updateParentDocument(filterHelper.filterDocument(getQuery(), parameters))));
    }
}
