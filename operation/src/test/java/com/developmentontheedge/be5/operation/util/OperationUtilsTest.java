package com.developmentontheedge.be5.operation.util;

import org.junit.Test;

import java.util.Arrays;

import static com.developmentontheedge.be5.operation.util.OperationUtils.selectedRows;
import static org.junit.Assert.*;

public class OperationUtilsTest
{
    @Test
    public void selectedRowsTest()
    {
        assertTrue(Arrays.equals(new String[]{"1", "2", "3"}, OperationUtils.selectedRows("1,2,3")));

        assertTrue(Arrays.equals(new String[]{"1"}, OperationUtils.selectedRows("1")));

        assertTrue(Arrays.equals(new String[]{}, OperationUtils.selectedRows("")));
    }
}
