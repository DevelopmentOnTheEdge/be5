package com.developmentontheedge.be5.operation.databasemodel;


import com.developmentontheedge.be5.operation.databasemodel.impl.ReferenceNotFoundException;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Map;

public interface RecordModel extends DynamicPropertySet
{
    
    void remove();

    void update(String propertyName, Object value);

    void update(Map<String, Object> values);

    String getId();

    Object invokeMethod(String methodName, Object... arguments);
    
    /**
     * Obtains entity reference and returns entity model with conditions by reference
     * @param name entity name
     * @return entity model with reference condition
     * @throws ReferenceNotFoundException if column reference is missing
     */
    @Deprecated
    EntityModelWithCondition getEntity(String name);
    
}
