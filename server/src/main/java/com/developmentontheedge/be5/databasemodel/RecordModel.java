package com.developmentontheedge.be5.databasemodel;


import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;

public interface RecordModel extends DynamicPropertySet
{
    
    void remove();

    void update(String propertyName, String value);

    void update(Map<String, String> values);

    Long getId();

    Object invokeMethod(String methodName, Object... arguments);

}
