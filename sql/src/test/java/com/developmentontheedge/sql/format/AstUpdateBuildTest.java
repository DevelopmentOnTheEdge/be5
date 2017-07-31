package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstFieldReference;
import com.developmentontheedge.sql.model.AstUpdate;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class AstUpdateBuildTest
{

    @Test
    public void test()
    {
        AstUpdate update = Ast.update("users").set(Collections.singletonMap("name", "Test"));

        assertEquals("UPDATE users SET name ='Test'", update.format());
    }

    @Test
    public void testAstFieldReference()
    {
        AstUpdate update = Ast.update("users").set(Collections.singletonMap(new AstFieldReference("___name", true), "Test"));

        assertEquals("UPDATE users SET \"___name\"='Test'", update.format());
    }

    @Test
    public void testEscapedColumns()
    {
        AstUpdate update = Ast.update("users").set(Collections.singletonMap("___name", "Test"));

        assertEquals("UPDATE users SET \"___name\"='Test'", update.format());
    }

    @Test
    public void testMany()
    {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("count", 4);
        map.put("name", "?");

        AstUpdate update = Ast.update("users").set(map);

        assertEquals("UPDATE users SET count = 4, name =?", update.format());
    }

    @Test
    public void testWhere()
    {
        AstUpdate update = Ast.update("users").set(Collections.singletonMap("name", "Test"))
                .where(Collections.singletonMap("name", "test"));

        assertEquals("UPDATE users SET name ='Test' WHERE name =?", update.format());
    }

}