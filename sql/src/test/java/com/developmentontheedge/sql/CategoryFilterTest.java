package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.sql.format.CategoryFilter;
import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;

public class CategoryFilterTest
{
    @Test
    public void testCategoryFilter()
    {
        AstStart start = SqlQuery.parse( "SELECT t.a, q.b FROM myTable t, otherTable q JOIN oneMoreTable a ON (a.ID=q.ID) WHERE t.b > 2");
        new CategoryFilter( "myTable", "ID", 123 ).apply( start );
        assertEquals("SELECT t.a, q.b FROM myTable t "
                + "INNER JOIN classifications ON classifications.categoryID = 123 "
                + "AND classifications.recordID = t.ID, otherTable q "
                + "INNER JOIN oneMoreTable a ON (a.ID = q.ID) "
                + "WHERE t.b > 2", new Formatter().format( start, new Context(Dbms.MYSQL), new DefaultParserContext() ));
    }
}
