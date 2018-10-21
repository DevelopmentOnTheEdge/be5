package com.developmentontheedge.sql.model;

import org.junit.Test;

import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_EQ;
import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_LOWER;
import static com.developmentontheedge.sql.model.DefaultParserContext.FUNC_UPPER;
import static org.junit.Assert.assertEquals;


public class DefaultParserContextTest
{
    @Test
    public void testUpper()
    {
        AstStart query = SqlQuery.parse("SELECT * FROM test");
        AstFunNode node = FUNC_UPPER.node(new AstFieldReference("test", "name"));
        AstFunNode node2 = FUNC_LOWER.node(new AstStringConstant("test"));
        AstFunNode eq = FUNC_EQ.node(node, node2);
        AstWhere where = new AstWhere();
        where.addChild(eq);
        ((AstSelect) query.getQuery().child(0)).where(where);
        assertEquals("SELECT * FROM test WHERE UPPER(test.name) = LOWER('test')", query.format());
    }

    @Test
    public void testReverse()
    {
        String query = "SELECT REVERSE(path) FROM test";
        assertEquals(query, SqlQuery.parse(query).format());
    }
}