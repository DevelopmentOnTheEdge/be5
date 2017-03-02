package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.sql.format.BasicQueryContext;
import com.developmentontheedge.sql.format.QueryContext;

public class BasicQueryContextTest
{
    @Test
    public void testBuilder()
    {
        QueryContext context = new BasicQueryContext.Builder()
            .parameter( "foo", "bar" )
            .sessionVar( "var", "value" )
            .build();
        
        assertEquals( "bar", context.getParameter( "foo" ) );
        assertEquals( "value", context.getSessionVariable( "var" ) );
        assertNull( context.getSessionVariable( "test" ) );
    }
}
