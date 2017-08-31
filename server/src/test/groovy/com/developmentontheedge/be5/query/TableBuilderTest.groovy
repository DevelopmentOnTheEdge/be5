package com.developmentontheedge.be5.query

import com.developmentontheedge.be5.components.impl.model.TableModel
import com.developmentontheedge.be5.test.AbstractProjectTest
import org.junit.Test


import static org.junit.Assert.assertEquals

class TableBuilderTest extends AbstractProjectTest
{
    class TestTable extends TableBuilderSupport
    {
        @Override
        TableModel getTable()
        {
            addColumns("name", "value")

            addRow(cells("a1", "b1"))
            addRow(cells("a2", "b2"))

            return table(columns, rows)
        }
    }

    @Test
    void getColumnsTest() throws Exception
    {
        def tableBuilder = new TestTable().initialize(
                injector.getMeta().getQueryIgnoringRoles("testtableAdmin", "All records"),
                new HashMap<>(),
                getMockRequest("")
        )
        TableModel table = tableBuilder.getTable()

        assertEquals"name", table.getColumns().get(0).getName()
        assertEquals"name", table.getColumns().get(0).getTitle()
        assertEquals"value", table.getColumns().get(1).getName()

        assertEquals"a1", table.getRows().get(0).getCells().get(0).getContent()
        assertEquals"b2", table.getRows().get(1).getCells().get(1).getContent()
    }

}