package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstReplacementParameter;
import org.junit.Test;

import static org.junit.Assert.*;

public class AstInsertBuildTest {

    @Test
    public void test()
    {
        AstInsert insert = Ast.insert("users").fields(new AstFieldReference("name")).values(new AstReplacementParameter());

        assertEquals("INSERT INTO users (name) VALUES (?)", insert.format());
    }

    @Test
    public void testString()
    {
        AstInsert insert = Ast.insert("users").fields("name", "value").values("Test", 1);

        assertEquals("INSERT INTO users (name, value) VALUES ('Test', 1)", insert.format());
    }

    @Test
    public void testStringReplacementParameter()
    {
        AstInsert insert = Ast.insert("users").fields("name", "value").values("?", "?");

        assertEquals("INSERT INTO users (name, value) VALUES (?, ?)", insert.format());
    }

    @Test(expected = ClassCastException.class)
    public void testStringError()
    {
        AstInsert insert = Ast.insert("users").fields(3).values("Test");

        assertEquals("INSERT INTO users (name) VALUES (?)", insert.format());
    }

}