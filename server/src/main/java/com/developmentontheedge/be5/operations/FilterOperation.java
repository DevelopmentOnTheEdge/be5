package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.components.DocumentGenerator;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.components.FrontendConstants.*;


public class FilterOperation extends OperationSupport
{
    @Inject private DocumentGenerator documentGenerator;

    @Override
    public Object getLayout()
    {
        return Collections.singletonMap("type", "modal");
    }

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), presetValues);

        List<String> searchPresets = new ArrayList<>();
        if(!presetValues.containsKey(SEARCH_PARAM))
        {
            searchPresets.addAll(dps.asMap().entrySet()
                    .stream().filter(x -> x.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toList())
            );
        }
        else
        {
            if(presetValues.get(SEARCH_PRESETS_PARAM) != null)
            {
                searchPresets.addAll(Arrays.asList(((String) presetValues.get(SEARCH_PRESETS_PARAM)).split(",")));
            }
        }

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if(searchPresets.contains(property.getName()))property.setReadOnly(true);
        }

        DynamicProperty searchPresetsProperty = new DynamicProperty(SEARCH_PRESETS_PARAM, String.class, String.join(",", searchPresets));
        searchPresetsProperty.setReadOnly(true);
        searchPresetsProperty.setCanBeNull(true);
        searchPresetsProperty.setHidden(true);
        dps.add(searchPresetsProperty);

        DynamicProperty searchParamProperty = new DynamicProperty(SEARCH_PARAM, Boolean.class, true);
        searchParamProperty.setReadOnly(true);
        searchParamProperty.setCanBeNull(true);
        searchParamProperty.setHidden(true);
        dps.add(searchParamProperty);


        return dpsHelper.setOperationParams(dps, context.getOperationParams());
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        Query query = meta.getQuery(getInfo().getEntityName(), context.getQueryName(), userInfo.getCurrentRoles());

        TablePresentation table = documentGenerator.getTable(query,
                dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters));

        setResult(OperationResult.table(table));
    }
}
