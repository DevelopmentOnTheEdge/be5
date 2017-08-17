package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDelete;
import com.developmentontheedge.sql.model.AstWhere;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;

public class DeleteTest {

    @Test
    public void selectAll()
    {
        AstDelete sql = Ast.delete("users");
        assertEquals("DELETE FROM users",
                sql.format());
    }

    @Test
    public void selectWhere()
    {
        AstDelete sql = Ast.delete("users").where(Collections.singletonMap("name", "test"));
        assertEquals("DELETE FROM users WHERE name =?",
                sql.format());
    }

    @Test
    public void selectWhereEmpty()
    {
        AstDelete sql = Ast.delete("users").where(new HashMap<>());
        assertEquals("DELETE FROM users",
                sql.format());
    }

    @Test
    public void whereIN()
    {
        AstDelete sql = Ast.delete("users").where(AstWhere.ofInPredicate("ID", 3));
        assertEquals("DELETE FROM users WHERE ID IN (?, ?, ?)",
                sql.format());

        sql = Ast.delete("users").whereInPredicate("ID", 3);
        assertEquals("DELETE FROM users WHERE ID IN (?, ?, ?)",
                sql.format());
    }
}