package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDelete;
import org.junit.Test;

import java.util.Collections;

import static com.developmentontheedge.sql.model.AstWhere.NOT_NULL;
import static org.junit.Assert.assertEquals;


public class AstWhereTest
{
    @Test
    public void simple()
    {
        AstDelete sql = Ast.delete("users").where(Collections.singletonMap("name", "test"));
        assertEquals("DELETE FROM users WHERE name = ?",
                sql.format());
    }

    @Test
    public void nullValue()
    {
        AstDelete sql = Ast.delete("users").where(Collections.singletonMap("name", null));
        assertEquals("DELETE FROM users WHERE name IS NULL",
                sql.format());

        AstDelete sql3 = Ast.delete("users").where(Collections.singletonMap("name", NOT_NULL));
        assertEquals("DELETE FROM users WHERE name IS NOT NULL",
                sql3.format());
    }

}