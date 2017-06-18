package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstReplacementParameter;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.*;

public class AstInsertBuildTest {

    @Test
    public void test()
    {
        AstInsert insert = Ast.insert(new AstFieldReference("name")).values(new AstReplacementParameter());

        String query = "INSERT INTO (name) VALUES (?)";
        AstStart parse = SqlQuery.parse(query);
        assertEquals("INSERT INTO (name) VALUES (?)", insert.format());
    }

    @Test
    public void testString()
    {
        AstInsert insert = Ast.insert("name", "value").values("Test", 1);

        assertEquals("INSERT INTO (name, value) VALUES ('Test', 1)", insert.format());
    }

    @Test
    public void testStringReplacementParameter()
    {
        AstInsert insert = Ast.insert("name", "value").values("?", "?");

        assertEquals("INSERT INTO (name, value) VALUES (?, ?)", insert.format());
    }

    @Test(expected = ClassCastException.class)
    public void testStringError()
    {
        AstInsert insert = Ast.insert(3).values("Test");

        assertEquals("INSERT INTO (name) VALUES (?)", insert.format());
    }

}