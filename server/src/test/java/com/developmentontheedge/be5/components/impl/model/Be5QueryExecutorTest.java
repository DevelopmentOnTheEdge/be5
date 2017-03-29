package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Be5QueryExecutorTest
{

    @Test
    public void testCountWrapper()
    {
        AstStart ast = SqlQuery.parse("SELECT\n" +
                "users.user_name AS \"Login\"\n" +
                "FROM\n" +
                "public.users");

        Be5QueryExecutor.countFromQuery(ast.getQuery());

        assertEquals("SELECT COUNT(*) FROM " + "(" +
                "SELECT\n" +
                "users.user_name AS \"Login\"\n" +
                "FROM\n" +
                "public.users" +
                ") " +"AS \"data\"", ast.format());
    }
}
