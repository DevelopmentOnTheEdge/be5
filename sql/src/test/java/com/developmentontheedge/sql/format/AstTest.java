package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstDerivedColumn;
import com.developmentontheedge.sql.model.AstSelect;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AstTest {

    @Test
    public void selectCount()
    {
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users");
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users",
                users.format());
    }

    @Test
    public void selectAll()
    {
        AstSelect users = Ast.select(AstDerivedColumn.ALL).from("users");
        assertEquals("SELECT * FROM users",
                users.format());
    }

    @Test
    public void selectWhere()
    {
        Map<String, String> name = Collections.singletonMap("name", "test");
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users").where(name);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name =?",
                users.format());
    }

    @Test
    public void selectWhereEmpty()
    {
        Map<String, String> name = new HashMap<>();
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users").where(name);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users",
                users.format());
    }

    @Test
    public void selectWhere2()
    {
        Map<String, String> names = new HashMap<>();
        names.put("name", "test");
        names.put("name2", "test2");
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name =? AND name2 =?",
                users.format());
    }

    @Test
    public void selectWhereLike()
    {
        Map<String, String> names = new HashMap<>();
        names.put("name", "test%");
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name LIKE ?",
                users.format());

        names.clear();
        names.put("name", "%test");
        AstSelect users2 = Ast.select(AstDerivedColumn.COUNT).from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name LIKE ?",
                users2.format());
    }

    @Test
    public void selectWhereNotNull()
    {
        Map<String, String> names = new HashMap<>();
        names.put("name", "null");
        AstSelect users = Ast.select(AstDerivedColumn.COUNT).from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name IS NULL",
                users.format());

        names.clear();
        names.put("name", "notNull");

        AstSelect users2 = Ast.select(AstDerivedColumn.COUNT).from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name IS NOT NULL",
                users2.format());
    }

}