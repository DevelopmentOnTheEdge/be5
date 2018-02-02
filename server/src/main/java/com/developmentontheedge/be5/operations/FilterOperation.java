package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class FilterOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        dpsHelper.addDpExcludeAutoIncrement(dps, getInfo().getEntity(), presetValues);

        List<String> searchPresets = new ArrayList<>();
        if(presetValues.containsKey("_search_presets_"))
        {
            searchPresets.addAll(Arrays.asList(((String)presetValues.get("_search_presets_")).split(",")));
        }
        else
        {
            searchPresets.addAll(presetValues.keySet());
        }

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if(searchPresets.contains(property.getName()))property.setReadOnly(true);
        }

        return dps;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception
    {
        Query query = meta.getQuery(getInfo().getEntityName(), getInfo().getQueryName(), userInfo.getCurrentRoles());

        //todo documentGenerator.getTableModel(
        //        meta.getQuery("testtable", "All records", Collections.singletonList("Guest")), new HashMap<>())

        //todo возвращать OperationResult 'filterResult' с параметрами фильтра
        //и отфильтрованую таблицу в JsonApiModel.included

//        DynamicPropertySet dps = (DynamicPropertySet) parameters;
//        Map<String, String> params = new HashMap<>();

//        for (DynamicProperty property : dps)
//        {
//            if(property.getValue() != null && !property.getValue().toString().isEmpty())//todo utils?
//            {
//                params.put(property.getName(), property.getValue().toString());
//            }
//        }

//        setResult(OperationResult.redirect(
//                new HashUrl(FrontendConstants.TABLE_ACTION, getInfo().getEntity().getName(), context.queryName)
//                        .named(params)
//        ));

//        addRedirectParams(params);
//        setResultRedirectThisOperation();
    }
}
