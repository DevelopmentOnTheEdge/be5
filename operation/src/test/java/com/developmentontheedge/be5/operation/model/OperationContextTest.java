package com.developmentontheedge.be5.operation.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;


public class OperationContextTest
{
    @Test
    public void getRecords()
    {
        OperationContext test = new OperationContext(new Long[]{1L, 2L}, "test", Collections.emptyMap());
        assertArrayEquals(new Long[]{1L, 2L}, test.getRecords());

        OperationContext test2 = new OperationContext(new String[]{"1", "2"}, "test", Collections.emptyMap());
        assertArrayEquals(new String[]{"1", "2"}, test2.getRecords());
    }

    @Test
    public void getRecord()
    {
        OperationContext test = new OperationContext(new Long[]{1L}, "test", Collections.emptyMap());
        assertEquals(1L, (long) test.getRecord());

        OperationContext test2 = new OperationContext(new String[]{"1"}, "test", Collections.emptyMap());
        assertEquals("1", test2.getRecord());
    }

    @Test(expected = IllegalStateException.class)
    public void getRecordError()
    {
        OperationContext test = new OperationContext(new Long[]{1L, 2L}, "test", Collections.emptyMap());
        assertEquals(1L, (long) test.getRecord());
    }

}