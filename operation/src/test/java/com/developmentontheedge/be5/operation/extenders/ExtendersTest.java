package com.developmentontheedge.be5.operation.extenders;

import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ExtendersTest extends OperationsSqlMockProjectTest
{
    @Before
    public void setUp()
    {
        DbServiceMock.clearMock();
    }

    @Test
    public void testInsertWithExtender()
    {
        assertEquals(OperationResult.redirect("table/testtable/All records"),
                executeOperation("testtable", "All records", "InsertWithExtender", "",
                        ImmutableMap.of("name", "test", "value", 1L)).getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvoke' WHERE 1=2");

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", 1L);

        verify(DbServiceMock.mock).update("update testTable name = 'postInvoke' WHERE 1=2");

        verify(DbServiceMock.mock, times(2)).update(anyString());

        verify(DbServiceMock.mock, times(1)).insert(anyString(), anyVararg());
    }

    @Test
    public void testInsertWithSkipExtender()
    {
        assertEquals(OperationResult.finished("Skip invoke"),
                executeOperation("testtable", "All records", "InsertWithSkipExtender", "",
                        ImmutableMap.of("name", "test", "value", 1L)).getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvokeBeforeSkip' WHERE 1=2");

        verify(DbServiceMock.mock, times(1)).update(anyString());

        verify(DbServiceMock.mock, times(0)).insert(anyString(), anyVararg());
    }

    @Test
    public void testInsertWithGroovyExtender()
    {
        assertEquals(OperationResult.redirect("table/testtable/All records"),
                executeOperation("testtable", "All records", "InsertWithGroovyExtender", "",
                        ImmutableMap.of("name", "test", "value", 1L)).getSecond());

        verify(DbServiceMock.mock).update("update testTable name = 'preInvokeBeforeSkipGroovy' WHERE 1=2");

        verify(DbServiceMock.mock).insert("INSERT INTO testtable (name, value) " +
                "VALUES (?, ?)", "test", 1L);
    }
}
