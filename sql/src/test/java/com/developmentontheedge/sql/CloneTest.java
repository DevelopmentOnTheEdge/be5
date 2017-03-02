package com.developmentontheedge.sql;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.sql.format.Context;
import com.developmentontheedge.sql.format.Dbms;
import com.developmentontheedge.sql.format.Formatter;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;

public class CloneTest
{
    @Test
    public void testAstClone()
    {
        AstStart start = SqlQuery.parse( "SELECT * FROM table1, table2 WHERE col = 'value'" );
        AstStart clone = start.clone();
        assertEquals(start.jjtGetNumChildren(), clone.jjtGetNumChildren());
        assertEquals(start.dump(), clone.dump());
        ((AstSelect)clone.getQuery().child( 0 )).getFrom().removeChild( 1 );
        // TODO: Current expected value is incorrect and should be fixed when formatter will be improved
        assertEquals( "SELECT * FROM table1 WHERE col = 'value'",
                new Formatter().format( clone, new Context( Dbms.ORACLE ), new DefaultParserContext() ) );
        assertEquals( "SELECT * FROM table1, table2 WHERE col = 'value'",
                new Formatter().format( start, new Context( Dbms.ORACLE ), new DefaultParserContext() ) );
    }
}
