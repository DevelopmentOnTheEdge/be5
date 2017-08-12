package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TableSupportTest extends AbstractProjectTest
{
    class TestTable extends TableBuilderSupport
    {
        @Override
        public TableModel getTable()
        {
            addColumns("name", "value");

            addRow(cells("a1", "b1"));
            addRow(cells("a2", "b2"));

            return table(columns, rows);
        }
    }

    @Test
    public void getColumnsTest() throws Exception
    {
        TableModel table = new TestTable().initialize(
                injector.getMeta().getQueryIgnoringRoles("testtableAdmin", "All records"),
                new HashMap<>(),
                getMockRequest(""),
                injector
        ).getTable();

        assertEquals("name", table.getColumns().get(0).getName());
        assertEquals("name", table.getColumns().get(0).getTitle());
        assertEquals("value", table.getColumns().get(1).getName());
    }

}