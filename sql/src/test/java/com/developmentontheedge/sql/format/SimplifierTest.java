package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.*;


public class SimplifierTest
{
    @Test
    public void simple()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE name = ?");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE name = ?", sql.getQuery().toString());
    }

    @Test
    public void simplifyAnd_true()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE TRUE AND name = ?");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE name = ?", sql.getQuery().toString());
    }

    @Test
    public void simplifyAnd_false()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE FALSE AND name = ?");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE FALSE ", sql.getQuery().toString());
    }

    @Test
    public void simplifyOr_true()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE TRUE OR name = ?");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users", sql.getQuery().toString());
    }

    @Test
    public void simplifyOr_false()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE FALSE OR name = ?");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE name = ?", sql.getQuery().toString());
    }

    @Test
    public void simplifyNot_true()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE NOT TRUE");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE FALSE", sql.getQuery().toString());
    }

    @Test
    public void simplifyNot_false()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE NOT FALSE");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users", sql.getQuery().toString());
    }

    @Test
    public void simplifyFunc_true()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE 1 = 1");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users", sql.getQuery().toString());
    }

    @Test
    public void simplifyFunc_false()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE 1 != 1");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE FALSE", sql.getQuery().toString());
    }

    @Test
    public void simplifyParenthesis()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE (TRUE AND name = ?)");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE name = ?", sql.getQuery().toString());
    }

    @Test
    public void simplifyInPredicateConstant()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE 'name' IN ('foo', 'name')");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users", sql.getQuery().toString());
    }

    @Test
    public void simplifyInPredicateColumnName()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE name IN ('foo', name)");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users", sql.getQuery().toString());
    }

    @Test
    public void simplifyInPredicateColumnNameAndConstant()
    {
        AstStart sql = SqlQuery.parse("SELECT * FROM users WHERE name IN ('foo', 'name')");
        Simplifier.simplify(sql);

        assertEquals("SELECT * FROM users WHERE name IN ('foo', 'name')", sql.getQuery().toString());
    }
}