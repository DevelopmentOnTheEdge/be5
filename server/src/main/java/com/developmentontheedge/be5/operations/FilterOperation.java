package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.components.DocumentGenerator;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
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

        Map<String, Object> operationParams = context.getOperationParams();

        List<String> searchPresets = new ArrayList<>();
        if(!operationParams.containsKey(SEARCH_PARAM))
        {
            searchPresets.addAll(dps.asMap().entrySet()
                    .stream().filter(x -> x.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toList())
            );
        }
        else
        {
            if(operationParams.get(SEARCH_PRESETS_PARAM) != null)
            {
                searchPresets.addAll(Arrays.asList(((String) operationParams.get(SEARCH_PRESETS_PARAM)).split(",")));
            }
        }

        dpsHelper.setValues(dps, operationParams);
        dpsHelper.setValues(dps, presetValues);

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if(searchPresets.contains(property.getName()))property.setReadOnly(true);
        }

        dps.add(new DynamicPropertyBuilder( SEARCH_PRESETS_PARAM, String.class)
                .value(String.join(",", searchPresets))
                .readonly()
                .nullable()
                .hidden()
                .get());

        dps.add(new DynamicPropertyBuilder( SEARCH_PARAM, Boolean.class)
                .value(true)
                .readonly()
                .nullable()
                .hidden()
                .get());

        return dps;
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
