package com.developmentontheedge.sql;

import com.developmentontheedge.sql.format.ColumnAdder;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnAdderTest
{
    @Test
    public void testColumnAdder()
    {
        check("table", "ID", "SELECT foo, bar FROM table", "SELECT table.ID AS \"___ID\", foo, bar FROM table");
        check("table", "ID", "SELECT foo, bar FROM table t", "SELECT t.ID AS \"___ID\", foo, bar FROM table t");
        check("table",
                "ID",
                "SELECT foo, bar FROM table t WHERE t.x = 1 UNION SELECT foo, bar FROM table q WHERE t.x = 2",
                "SELECT t.ID AS \"___ID\", foo, bar FROM table t WHERE t.x = 1 UNION SELECT q.ID AS \"___ID\", foo, bar FROM table q WHERE t.x = 2");
        check("table", "ID", "SELECT t2.foo, t.bar FROM table2 t2 INNER JOIN table t ON t.test = t2.test",
                "SELECT t.ID AS \"___ID\", t2.foo, t.bar FROM table2 t2 INNER JOIN table t ON t.test = t2.test");

        check("table", "ID", "SELECT t.ID AS \"___ID\", t2.foo, t.bar FROM table2 t2 INNER JOIN table t ON t.test = t2.test",
                "SELECT t.ID AS \"___ID\", t2.foo, t.bar FROM table2 t2 INNER JOIN table t ON t.test = t2.test");
    }

    protected void check(String tableName, String columnName, String query, String expected)
    {
        AstStart ast = parse(query);
        new ColumnAdder().addColumn(ast, tableName, columnName, "___ID");
        assertEquals(expected, ast.format());
    }

    protected AstStart parse(String input)
    {
        return SqlQuery.parse(input);
    }
}
