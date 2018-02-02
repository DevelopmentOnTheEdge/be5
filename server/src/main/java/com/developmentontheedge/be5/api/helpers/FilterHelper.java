package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import one.util.streamex.EntryStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.components.FrontendConstants.SEARCH_PRESETS_PARAM;


public class FilterHelper
{
    private static final List<String> keywords = Arrays.asList("category", SEARCH_PARAM, SEARCH_PRESETS_PARAM);

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
