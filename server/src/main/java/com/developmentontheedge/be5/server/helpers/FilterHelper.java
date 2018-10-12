package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.databasemodel.util.DpsUtils;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySet;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.SEARCH_PRESETS_PARAM;


public class FilterHelper
{
    private final DpsHelper dpsHelper;
    private final DocumentGenerator documentGenerator;

    @Inject
    public FilterHelper(DpsHelper dpsHelper, DocumentGenerator documentGenerator)
    {
        this.dpsHelper = dpsHelper;
        this.documentGenerator = documentGenerator;
    }

    public <T extends DynamicPropertySet> T processFilterParams(T dps, Map<String, Object> presetValues,
                                                                Map<String, Object> operationParams)
    {
        Map<String, Object> filterPresetValues = new HashMap<>(operationParams);
        filterPresetValues.putAll(presetValues);

        List<String> searchPresets = FilterUtil.getSearchPresetNames(filterPresetValues);

        for (DynamicProperty property : dps)
        {
            if (!property.getBooleanAttribute(BeanInfoConstants.LABEL_FIELD) && !property.isReadOnly())
            {
                property.setValue(null); //remove defaultValue
            }
        }

        DpsUtils.setValues(dps, filterPresetValues);

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if (searchPresets.contains(property.getName())) property.setReadOnly(true);
        }

        dps.add(new DynamicPropertyBuilder(SEARCH_PRESETS_PARAM, String.class)
                .value(searchPresets.size() > 0 ? String.join(",", searchPresets) : null)
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

    public JsonApiModel filterDocument(Query query, Object parameters)
    {
        return documentGenerator.getJsonApiModel(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters));
    }

}
