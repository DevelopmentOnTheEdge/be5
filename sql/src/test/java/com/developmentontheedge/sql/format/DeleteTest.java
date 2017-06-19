package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDelete;
import org.junit.Test;

import java.util.Collections;

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
}