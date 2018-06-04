package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AstDerivedColumnTest
{
    @Test
    public void setAlias()
    {
        AstDerivedColumn test = new AstDerivedColumn(new AstIdentifierConstant("name"));
        assertEquals("name", test.format());

        assertNull(null, test.getAlias());
        assertEquals("name", test.format());

        test.setAlias("Name");

        assertEquals("\"Name\"", test.getAlias());
        assertEquals("name AS \"Name\"", test.format());
    }

    @Test
    public void astAllColumnRefTest()
    {
        String sql = "select table.* from table";
        AstStart parse = SqlQuery.parse(sql);
        AstSelect astSelect = (AstSelect) parse.getQuery().child(0);

        assertEquals("table.*", ((AstDerivedColumn)astSelect.getSelectList().child(0)).getColumn());
    }
}