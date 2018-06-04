package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.beans.util.Beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

class TestHelpers
{
    static void checkEquality(BeModelElement e1, BeModelElement e2) throws Exception
    {
        assertEquals(e1, e2);
        assertFalse(e1.getCustomizableProperties().isEmpty());
        for(String prop : e1.getCustomizableProperties())
        {
            Class<?> type = Beans.getBeanPropertyType( e2, prop );
            if(type == String.class)
            {
                String oldValue = ( String ) Beans.getBeanPropertyValue( e2, prop );
                Beans.setBeanPropertyValue( e2, prop, oldValue+"a" );
                assertNotEquals( prop, e1, e2 );
                Beans.setBeanPropertyValue( e2, prop, oldValue );
                assertEquals( prop, e1, e2 );
            } else if(type == boolean.class)
            {
                boolean oldValue = ( boolean ) Beans.getBeanPropertyValue( e2, prop );
                Beans.setBeanPropertyValue( e2, prop, !oldValue );
                assertNotEquals( prop, e1, e2 );
                Beans.setBeanPropertyValue( e2, prop, oldValue );
                assertEquals( prop, e1, e2 );
            } else if(type == int.class)
            {
                int oldValue = ( int ) Beans.getBeanPropertyValue( e2, prop );
                Beans.setBeanPropertyValue( e2, prop, oldValue+1 );
                assertNotEquals( prop, e1, e2 );
                Beans.setBeanPropertyValue( e2, prop, oldValue );
                assertEquals( prop, e1, e2 );
            }
        }
        
    }
}
