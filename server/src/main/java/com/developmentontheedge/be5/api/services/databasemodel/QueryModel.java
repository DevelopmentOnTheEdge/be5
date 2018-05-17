package com.developmentontheedge.be5.api.services.databasemodel;


import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface QueryModel  {

    void each(BiConsumer<DynamicPropertySet, Integer> lambda);
    
    <T> List<T> collect(final BiFunction<DynamicPropertySet, Integer, T> lambda);

    List<DynamicPropertySet> collect();

//    CloseableIterator<DynamicPropertySet> getIterator();
//
//    @Deprecated
//    @Experimental
//    @DirtyRealization( comment = "I don't like this realization of .map" )
//    <T> CloseableIterator<T> getIterator(Function<DynamicPropertySet, T> func);

}
