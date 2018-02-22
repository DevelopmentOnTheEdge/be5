package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.components.DocumentGenerator;
import com.developmentontheedge.be5.components.impl.model.Be5QueryExecutor;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import one.util.streamex.EntryStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PRESETS_PARAM;


public class FilterHelper
{
    private static final Logger log = Logger.getLogger(FilterHelper.class.getName());

    private static final List<String> keywords = Arrays.asList("category", SEARCH_PARAM, SEARCH_PRESETS_PARAM);

    private final DpsHelper dpsHelper;
    private final Meta meta;
    private final DocumentGenerator documentGenerator;

    public FilterHelper(DpsHelper dpsHelper, Meta meta, DocumentGenerator documentGenerator)
    {
        this.dpsHelper = dpsHelper;
        this.meta = meta;
        this.documentGenerator = documentGenerator;
    }

    public <T extends DynamicPropertySet> T processFilterParams(T dps, Map<String, Object> presetValues,
                                                                Map<String, String> operationParams)
    {
        Map<String, Object> filterPresetValues = new HashMap<>(operationParams);
        filterPresetValues.putAll(presetValues);

        List<String> searchPresets = new ArrayList<>();
        if(!filterPresetValues.containsKey(SEARCH_PARAM))
        {
            searchPresets.addAll(
                    presetValues.entrySet()
                            .stream()
                            .filter(x -> x.getValue() != null)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList())
            );
        }
        else
        {
            if(filterPresetValues.get(SEARCH_PRESETS_PARAM) != null)
            {
                searchPresets.addAll(Arrays.asList(((String) filterPresetValues.get(SEARCH_PRESETS_PARAM)).split(",")));
            }
        }

        dpsHelper.setValues(dps, filterPresetValues);

        for (DynamicProperty property : dps)
        {
            property.setCanBeNull(true);
            if(searchPresets.contains(property.getName()))property.setReadOnly(true);
        }

        dps.add(new DynamicPropertyBuilder(SEARCH_PRESETS_PARAM, String.class)
                .value(String.join(",", searchPresets))
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

    public TablePresentation filterTable(String entityName, String queryName, Object parameters)
    {
        Query query = meta.getQuery(entityName, queryName, UserInfoHolder.getCurrentRoles());

        return filterTable(query, parameters);
    }

    public TablePresentation filterTable(Query query, Object parameters)
    {
        return documentGenerator.getTable(query, dpsHelper.getAsMapStringValues((DynamicPropertySet) parameters));
    }

    public void applyFilters(AstStart ast, String mainEntityName, Map<String, Object> parameters)
    {
        Set<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toSet();

        Map<ColumnRef, Object> filters = EntryStream.of(parameters)
                .removeKeys(usedParams::contains)
                .removeKeys(keywords::contains)
                .mapKeys(k -> ColumnRef.resolve(ast, k.contains(".") ? k : mainEntityName + "." + k))
                .nonNullKeys().toMap();

        if(!filters.isEmpty())
        {
            new FilterApplier().addFilter(ast, filters);
        }
    }
}
