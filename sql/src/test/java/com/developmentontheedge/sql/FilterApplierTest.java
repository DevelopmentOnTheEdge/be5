package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.ColumnRef;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.FilterApplier;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import one.util.streamex.EntryStream;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class FilterApplierTest
{
    @Test
    public void testSimple()
    {
        AstStart query = SqlQuery.parse( "SELECT * FROM games g, city WHERE g.city = city.name" );
        Map<ColumnRef, Object> conditions = EntryStream.<String, Object>of( "city.country", "UK" )
                .mapKeys( key -> ColumnRef.resolve( query, key ) ).toCustomMap(LinkedHashMap::new);

        new FilterApplier().setFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games g, city WHERE city.country ='UK'",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testMultiple()
    {
        AstStart query = SqlQuery.parse( "SELECT * FROM games g, city WHERE g.city = city.name" );
        Map<ColumnRef, List<String>> conditions = Collections.singletonMap(ColumnRef.resolve( query, "city.country" ),
                Arrays.asList("A", "B"));

        new FilterApplier().setFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games g, city WHERE city.country IN ('A', 'B')",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testSetFilterApplier()
    {
        AstStart query = SqlQuery.parse( "SELECT * FROM games g, city WHERE g.city = city.name" );
        Map<ColumnRef, Object> conditions = EntryStream.<String, Object>of( "city.country", "UK", "games.yr", 2012 )
                .mapKeys( key -> ColumnRef.resolve( query, key ) ).toCustomMap(LinkedHashMap::new);

        new FilterApplier().setFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games g, city WHERE city.country ='UK' AND g.yr = 2012",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );

        AstStart query2 = SqlQuery.parse( "SELECT city.name, g.* FROM city INNER JOIN games g ON (g.city = city.name)" );
        new FilterApplier().setFilter( query2, getMapOfList(conditions) );

        assertEquals( "SELECT city.name, g.* FROM city INNER JOIN games g WHERE city.country ='UK' AND g.yr = 2012",
                new Formatter().format( query2, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );


        AstStart query3 = SqlQuery.parse( "SELECT * FROM city JOIN games g ON (g.city = city.name) JOIN games gm ON city.country ='UK'" );
        new FilterApplier().setFilter( query3, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM city INNER JOIN games g INNER JOIN games gm WHERE city.country ='UK' AND g.yr = 2012",
                new Formatter().format( query3, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testSetFilterApplierUnion()
    {
        AstStart query = SqlQuery.parse( "SELECT name FROM bbc WHERE name LIKE 'Z%' UNION SELECT name FROM actor WHERE name LIKE 'Z%'" );
        Map<ColumnRef, Object> conditions = Collections.singletonMap( ColumnRef.resolve( query, "name" ), "name" );
        new FilterApplier().setFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM (SELECT name FROM bbc UNION SELECT name FROM actor) tmp WHERE name ='name'",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );

//        conditions = Collections.singletonMap( ColumnRef.resolve( query, "name1" ), "name1" );
//        new FilterApplier().setFilter( query, conditions );
//        assertEquals( "SELECT * FROM (SELECT name FROM bbc UNION SELECT name FROM actor) tmp WHERE name1 ='name1'",
//                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testAddFilterApplier()
    {
        AstStart query = SqlQuery
                .parse( "SELECT * FROM games, city WHERE games.city = city.name AND city.country = 'UK'" );
        Map<ColumnRef, Object> conditions = Collections.singletonMap( ColumnRef.resolve( query, "games.yr" ), 2012 );
        new FilterApplier().addFilter( query, getMapOfList(conditions) );
        assertEquals( "SELECT * FROM games, city WHERE games.city = city.name AND city.country = 'UK' AND games.yr = 2012",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );

        query = SqlQuery.parse( "SELECT * FROM games RIGHT JOIN city ON (games.city = city.name) WHERE city.country ='UK'" );
        new FilterApplier().addFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games RIGHT JOIN city ON (games.city = city.name) WHERE city.country ='UK' AND games.yr = 2012",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );

        query = SqlQuery.parse( "SELECT * FROM games RIGHT JOIN city ON (games.city = city.name) WHERE city.country ='UK' OR city.active = 'yes'" );
        new FilterApplier().addFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games RIGHT JOIN city ON (games.city = city.name) WHERE ( city.country ='UK' OR city.active = 'yes') AND games.yr = 2012",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );

        query = SqlQuery.parse( "SELECT * FROM games, city ORDER BY 1" );
        new FilterApplier().addFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM games, city WHERE games.yr = 2012 ORDER BY 1",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    @Test
    public void testAddFilterApplierUnion() throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        AstStart query = SqlQuery.parse( "SELECT name FROM bbc b WHERE name LIKE 'Z%' UNION SELECT name FROM actor WHERE name LIKE 'Z%'" );
        Map<ColumnRef, Object> conditions = Collections.singletonMap( ColumnRef.resolve( query, "bbc.data" ), new java.sql.Date(format.parse("01-01-1900").getTime()) );
        new FilterApplier().addFilter( query, getMapOfList(conditions) );

        assertEquals( "SELECT * FROM (SELECT name FROM bbc b WHERE name LIKE 'Z%' UNION SELECT name FROM actor WHERE name LIKE 'Z%') tmp WHERE b.data ='1900-01-01'",
                new Formatter().format( query, new Context( Dbms.POSTGRESQL ), new DefaultParserContext() ) );
    }

    private Map<ColumnRef, List<Object>> getMapOfList(Map<ColumnRef, ?> parameters)
    {
        Map<ColumnRef, List<Object>> listParams = new HashMap<>();
        parameters.forEach((k,v) -> listParams.put(k, getParameterList(v)));

        return listParams;
    }

    @SuppressWarnings("unchecked")
    private List<Object> getParameterList(Object parameter)
    {
        if(parameter == null)return null;

        if(parameter instanceof List)
        {
            return (List<Object>) parameter;
        }
        else
        {
            return Collections.singletonList(parameter);
        }
    }
}
