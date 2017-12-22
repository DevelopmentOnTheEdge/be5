package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import one.util.streamex.EntryStream;

import java.util.Map;
import java.util.Set;


public class FilterHelper
{
    public void applyFilters(AstStart ast, String mainEntityName, Map<String, Object> parameters)
    {
        Set<String> usedParams = ast.tree().select(AstBeParameterTag.class).map(AstBeParameterTag::getName).toSet();

        Map<ColumnRef, Object> filters = EntryStream.of(parameters)
                .removeKeys(usedParams::contains)
                .removeKeys("category"::equals)
                .mapKeys(k -> ColumnRef.resolve(ast, k.contains(".") ? k : mainEntityName + "." + k))
                .nonNullKeys().toMap();

        if(!filters.isEmpty())
        {
            new FilterApplier().addFilter(ast, filters);
        }
    }
}
