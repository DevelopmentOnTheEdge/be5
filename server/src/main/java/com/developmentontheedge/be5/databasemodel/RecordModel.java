package com.developmentontheedge.be5.databasemodel;


import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;

public interface RecordModel extends DynamicPropertySet
{

    int remove();

    void update(String propertyName, String value);

    void update(Map<String, Object> values);

    Long getId();

    Object invokeMethod(String methodName, Object... arguments);

}
