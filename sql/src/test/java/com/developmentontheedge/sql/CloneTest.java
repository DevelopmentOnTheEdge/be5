package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.dbms.Context;
import com.developmentontheedge.sql.format.dbms.Dbms;
import com.developmentontheedge.sql.format.dbms.Formatter;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CloneTest
{
    @Test
    public void testAstClone()
    {
        AstStart start = SqlQuery.parse("SELECT * FROM table1, table2 WHERE col = 'value'");
        AstStart clone = start.clone();
        assertEquals(start.jjtGetNumChildren(), clone.jjtGetNumChildren());
        assertEquals(start.dump(), clone.dump());
        ((AstSelect) clone.getQuery().child(0)).getFrom().removeChild(1);
        // TODO: Current expected value is incorrect and should be fixed when formatter will be improved
        assertEquals("SELECT * FROM table1 WHERE col = 'value'",
                new Formatter().format(clone, new Context(Dbms.ORACLE)));
        assertEquals("SELECT * FROM table1, table2 WHERE col = 'value'",
                new Formatter().format(start, new Context(Dbms.ORACLE)));
    }
}
