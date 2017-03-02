package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.format.OrderByFilter;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;

public class OrderByFilterTest
{
    @Test
    public void testOrderByFilter()
    {
        AstStart start = SqlQuery.parse( "SELECT t.a, t.b, t.c AS foo FROM myTable t WHERE t.b > 2" );
        Map<String, String> columns = new HashMap<String, String>();
        columns.put( "t.a", "ASC" );
        columns.put( "foo", "DESC" );
        new OrderByFilter().apply( start, columns );
        assertEquals( "SELECT t.a, t.b, t.c AS foo FROM myTable t WHERE t.b > 2 ORDER BY 1 ASC, 3 DESC",
                new Formatter().format( start, new Context( Dbms.MYSQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testOrderByFilterUnion()
    {
        AstStart start = SqlQuery.parse( "SELECT name FROM bbc WHERE name LIKE 'Z%' UNION SELECT name FROM actor WHERE name LIKE 'Z%'" );
        Map<String, String> columns = Collections.singletonMap( "name", "DESC" );
        new OrderByFilter().apply( start, columns );
        assertEquals( "SELECT * FROM (SELECT name FROM bbc WHERE name LIKE 'Z%' UNION SELECT name FROM actor WHERE name LIKE 'Z%') "
                + "tmp ORDER BY 1 DESC", new Formatter().format( start, new Context( Dbms.MYSQL ), new DefaultParserContext() ) );
    }
}
