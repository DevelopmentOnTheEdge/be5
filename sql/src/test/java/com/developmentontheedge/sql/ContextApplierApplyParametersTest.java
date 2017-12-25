package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.BasicQueryContext;
import com.developmentontheedge.sql.format.ContextApplier;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * You must manually add quotes if you need them, or use 'safestr="yes"'
 */
public class ContextApplierApplyParametersTest
{
    @Test
    public void string()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table WHERE name = '<parameter:name />'" );

        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder()
                .parameter("name", "test")
                .build() );
        contextApplier.applyContext( start );

        assertEquals("SELECT * FROM table WHERE name = 'test'", start.getQuery().toString());
    }

    @Test
    public void digitString()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table WHERE name = '<parameter:name />'" );

        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder()
                .parameter("name", "123")
                .build() );
        contextApplier.applyContext( start );

        assertEquals("SELECT * FROM table WHERE name = '123'", start.getQuery().toString());
    }

    @Test
    public void useSafeStr()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table WHERE name = <parameter:name safestr=\"yes\" />" );

        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder()
                .parameter("name", "12's")
                .build() );
        contextApplier.applyContext( start );

        assertEquals("SELECT * FROM table WHERE name = '12''s'", start.getQuery().toString());
    }

    @Test
    public void number()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table WHERE totalSize = <parameter:totalSize />" );

        ContextApplier contextApplier = new ContextApplier( new BasicQueryContext.Builder()
                .parameter("totalSize", "123")
                .build() );
        contextApplier.applyContext( start );

        assertEquals("SELECT * FROM table WHERE totalSize = 123", start.getQuery().toString());
    }

}