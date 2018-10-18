package com.developmentontheedge.be5.databasemodel;

import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;

public interface RecordModel<T> extends DynamicPropertySet
{
    T getPrimaryKey();

    int remove();

    int update(String propertyName, Object value);

    int update(Map<String, ?> values);
}
