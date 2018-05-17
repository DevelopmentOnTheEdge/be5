package com.developmentontheedge.be5.api.services.databasemodel.groovy;

import com.developmentontheedge.be5.api.services.databasemodel.QueryModel;
import com.developmentontheedge.beans.DynamicPropertySet;


import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class QueryModelMetaClass extends ExtensionMethodsMetaClass
{
    public QueryModelMetaClass( Class<? extends QueryModel> theClass )
    {
        super( theClass );
    }

    public static <T> List<T> collect( QueryModel q, BiFunction<DynamicPropertySet, Integer, T> lambda )
    {
        return q.collect( lambda );
    }

    public static void each( QueryModel q, BiConsumer<DynamicPropertySet, Integer> lambda )
    {
        q.each( lambda );
    }
}
