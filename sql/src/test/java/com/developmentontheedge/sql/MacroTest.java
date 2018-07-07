package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.dbms.Context;
import com.developmentontheedge.sql.format.dbms.Dbms;
import com.developmentontheedge.sql.format.dbms.Formatter;
import com.developmentontheedge.sql.format.MacroExpander;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.ParserContext;
import com.developmentontheedge.sql.model.SqlParser;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MacroTest
{
    @Test
    public void testMacro()
    {
        SqlParser parser = new SqlParser();
        String input = "MACRO A(arg1=default, arg2=NOW()) \'<!--\' || CAST((arg2) AS CHAR) || \'-->\' || \'<a href=\"...\">\' || arg1 || \'</a>\' END";
        parser.parse(input);
        if (!parser.getMessages().isEmpty())
        {
            throw new IllegalArgumentException(String.join("\n", parser.getMessages()));
        }
        ParserContext context = parser.getContext();

        AstStart start = SqlQuery.parse("SELECT A(a, b) FROM table t", context);
        new MacroExpander().expandMacros(start);
        assertEquals("SELECT  \'<!--\' || TO_CHAR(( b))|| \'-->\' || \'<a href=\"...\">\' || a || \'</a>\'  FROM table t",
                new Formatter().format(start, new Context(Dbms.ORACLE), context));
        start = SqlQuery.parse("SELECT A(a) FROM table t", context);
        new MacroExpander().expandMacros(start);
        assertEquals("SELECT  \'<!--\' || TO_CHAR((SYSDATE))|| \'-->\' || \'<a href=\"...\">\' || a || \'</a>\'  FROM table t",
                new Formatter().format(start, new Context(Dbms.ORACLE), context));

        SqlParser newParser = new SqlParser();
        input = "MACRO B(arg1, arg2, arg3) arg1 || arg2 || A(arg3) END";
        newParser.setContext(context);
        newParser.parse(input);
        if (!newParser.getMessages().isEmpty())
        {
            throw new IllegalArgumentException(String.join("\n", newParser.getMessages()));
        }
        start = SqlQuery.parse("SELECT B(a, b, c) FROM table t", context);
        new MacroExpander().expandMacros(start);
        assertEquals("SELECT  a ||  b || '<!--' || TO_CHAR((SYSDATE))|| \'-->\' || '<a href=\"...\">\' ||  c || \'</a>\'   FROM table t",
                new Formatter().format(start, new Context(Dbms.ORACLE), context));
    }
}
