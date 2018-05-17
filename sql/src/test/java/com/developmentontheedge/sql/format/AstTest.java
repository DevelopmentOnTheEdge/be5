package com.developmentontheedge.sql.format;

import com.developmentontheedge.sql.model.AstSelect;
import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AstTest
{

    @Test
    public void selectCount()
    {
        AstSelect users = Ast.selectCount().from("users");
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users",
                users.format());
    }

    @Test
    public void selectAll()
    {
        AstSelect users = Ast.selectAll().from("users");
        assertEquals("SELECT * FROM users",
                users.format());
    }

    @Test
    public void selectColumns()
    {
        AstSelect users = Ast.select(ImmutableList.of("name", "email")).from("users");
        assertEquals("SELECT name, email FROM users",
                users.format());
    }

    @Test
    public void selectWhere()
    {
        Map<String, ? super Object> name = Collections.singletonMap("name", "test");
        AstSelect users = Ast.selectCount().from("users").where(name);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name = ?",
                users.format());
    }

    @Test
    public void selectWhereEmpty()
    {
        Map<String, ? super Object> name = new HashMap<>();
        AstSelect users = Ast.selectCount().from("users").where(name);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users",
                users.format());
    }

    @Test
    public void selectWhere2()
    {
        Map<String, ? super Object> names = new HashMap<>();
        names.put("name", "test");
        names.put("name2", "test2");
        AstSelect users = Ast.selectCount().from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name = ? AND name2 = ?",
                users.format());
    }

    @Test
    public void selectWhereLike()
    {
        Map<String, ? super Object> names = new HashMap<>();
        names.put("name", "test%");
        AstSelect users = Ast.selectCount().from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name LIKE ?",
                users.format());

        names.clear();
        names.put("name", "%test");
        AstSelect users2 = Ast.selectCount().from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name LIKE ?",
                users2.format());
    }

    /**
     * todo можно сделать какой-нибудь хак (ID IS NULL OR ( null = ? ) )
     */
    @Test
    @Ignore
    public void selectWhereNotNull()
    {
        Map<String, ? super Object> names = new HashMap<>();
        names.put("name", "null");
        AstSelect users = Ast.selectCount().from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name IS NULL",
                users.format());

        names.clear();
        names.put("name", "notNull");

        AstSelect users2 = Ast.selectCount().from("users").where(names);
        assertEquals("SELECT COUNT(*) AS \"count\" FROM users WHERE name IS NOT NULL",
                users2.format());
    }

}