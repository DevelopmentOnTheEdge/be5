package com.developmentontheedge.sql.format;

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
    public void testMany()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", 4);
        map.put("name", "?");

        AstUpdate update = Ast.update("users").set(map);

        assertEquals("UPDATE users SET count = 4, name =?", update.format());
    }

}