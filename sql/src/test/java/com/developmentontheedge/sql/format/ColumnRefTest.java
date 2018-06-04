package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnRefTest
{
    @Test
    public void toStringTest()
    {
        assertEquals("ColumnRef{table='users', name='name'}",
                new ColumnRef("users", "name").toString());
    }

    @Test
    public void resolveInDerivedColumns()
    {
        AstStart sql = SqlQuery.parse("SELECT name FROM users");
        ColumnRef resolve = ColumnRef.resolve(sql, "name");

        assertEquals(new ColumnRef(null, "name"), resolve);
    }

    @Test
    public void resolve()
    {
        AstStart sql = SqlQuery.parse("SELECT foo FROM users");
        ColumnRef resolve = ColumnRef.resolve(sql, "users.name");

        assertEquals(new ColumnRef("users", "name"), resolve);
    }

    @Test
    public void resolveWithAlias()
    {
        AstStart sql = SqlQuery.parse("SELECT u.foo FROM users u");
        ColumnRef resolve = ColumnRef.resolve(sql, "users.name");

        assertEquals(new ColumnRef("u", "name"), resolve);
    }

}