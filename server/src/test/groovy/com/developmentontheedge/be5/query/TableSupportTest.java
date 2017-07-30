package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TableSupportTest extends AbstractProjectTest
{
    class TestTable extends TableSupport
    {
        @Override
        public TableModel get()
        {
            return null;
        }
    }

    @Test
    public void getColumnsTest() throws Exception
    {
        TestTable test = (TestTable)new TestTable().initialize(
                injector.getMeta().getQueryIgnoringRoles("testtableAdmin", "All records"),
                new HashMap<>(),
                getMockRequest(""),
                injector
        );

        List<TableModel.ColumnModel> columns = test.getColumns("name", "value");
        assertEquals("name", columns.get(0).getName());
        assertEquals("name", columns.get(0).getTitle());
        assertEquals("value", columns.get(1).getName());
    }

}