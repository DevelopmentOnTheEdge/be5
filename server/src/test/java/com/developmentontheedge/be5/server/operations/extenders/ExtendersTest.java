package com.developmentontheedge.be5.server.operations.extenders;

import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.server.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ExtendersTest extends SqlMockOperationTest
{
    @Test
    public void testInsertWithExtender()
    {
        assertEquals(OperationResult.redirect("table/testtable/All records"),
                executeOperation("testtable", "All records", "InsertWithExtender", "",
                        "{'name':'test','value':'1'}").getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvoke' WHERE 1=2");

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", "1");

        verify(DbServiceMock.mock).update("update testTable name = 'postInvoke' WHERE 1=2");

        verify(DbServiceMock.mock, times(2)).update(anyString());

        verify(DbServiceMock.mock, times(1)).insert(anyString(), anyVararg());
    }

    @Test
    public void testInsertWithSkipExtender()
    {
        assertEquals(OperationResult.finished("Skip invoke"),
                executeOperation("testtable", "All records", "InsertWithSkipExtender", "",
                        "{'name':'test','value':'1'}").getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvokeBeforeSkip' WHERE 1=2");

        verify(DbServiceMock.mock, times(1)).update(anyString());

        verify(DbServiceMock.mock, times(0)).insert(anyString(), anyVararg());
    }

    @Test
    public void testInsertWithGroovyExtender()
    {
        assertEquals(OperationResult.redirect("table/testtable/All records"),
                executeOperation("testtable", "All records", "InsertWithGroovyExtender", "",
                        "{'name':'test','value':'1'}").getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvokeBeforeSkipGroovy' WHERE 1=2");

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", "1");
    }
}