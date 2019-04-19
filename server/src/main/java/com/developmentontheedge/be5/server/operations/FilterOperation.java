package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.be5.server.services.document.DocumentGenerator;
import com.developmentontheedge.be5.util.FilterUtil;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM;
import static com.developmentontheedge.be5.server.FrontendActions.closeMainModal;
import static com.developmentontheedge.be5.server.FrontendActions.updateParentDocument;


public class FilterOperation extends OperationSupport
{
    @Inject
    protected DocumentGenerator documentGenerator;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = getFilterParameters(getPresetValues(presetValues));
        Collection<String> searchPresets = FilterUtil.getSearchPresetNames(context.getParams());

        for (DynamicProperty property : dps)
        {
            if (!property.getBooleanAttribute(BeanInfoConstants.LABEL_FIELD) && !property.isReadOnly())
            {
                property.setValue(null); //remove defaultValue
            }
        }

        DpsUtils.setValues(dps, context.getParams());
        DpsUtils.setValues(dps, presetValues);

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if (searchPresets.contains(property.getName())) property.setReadOnly(true);
        }

        dps.add(new DynamicPropertyBuilder(SEARCH_PRESETS_PARAM, String.class)
                .value(FilterUtil.getSearchPresetParam(searchPresets))
                .readonly()
                .nullable()
                .hidden()
                .get());

        dps.add(new DynamicPropertyBuilder(SEARCH_PARAM, Boolean.class)
                .value(true)
                .readonly()
                .nullable()
                .hidden()
                .get());

        return dps;
    }

    private Map<String, Object> getPresetValues(Map<String, Object> values)
    {
        Map<String, Object> presetValues = new HashMap<>(FilterUtil.getFilterParams(getContext().getParams()));
        presetValues.putAll(values);
        return presetValues;
    }

    protected DynamicPropertySet getFilterParameters(Map<String, Object> presetValues) throws Exception
    {
        return getBaseParameters();
    }

    protected DynamicPropertySet getBaseParameters() throws Exception
    {
        return dpsHelper.addDpExcludeAutoIncrement(new DynamicPropertySetSupport(),
                getInfo().getModel(), context.getParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Map<String, Object> params = dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters);
        params.putAll(FilterUtil.getContextParams(context.getParams()));
        setResultFinished(
                updateParentDocument(documentGenerator.getDocument(getQuery(), params)),
                closeMainModal()
        );
    }
}
