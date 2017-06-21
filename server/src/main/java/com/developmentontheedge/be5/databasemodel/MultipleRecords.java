package com.developmentontheedge.be5.databasemodel;

import java.util.Map;

public interface MultipleRecords<T> {

    T get();

    T get(String... call);

    T get(Map<String, ?> values);
    
//    T get( String queryName, Map<String, String> values );
    
}
