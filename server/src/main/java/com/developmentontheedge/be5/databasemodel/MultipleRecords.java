package com.developmentontheedge.be5.databasemodel;

import java.util.List;
import java.util.Map;


public interface MultipleRecords<T>
{
    List<RecordModel> get();

    List<RecordModel> get(Map<String, ? super Object> conditions);
    
//    T get( String queryName, Map<String, String> values );
    
}
