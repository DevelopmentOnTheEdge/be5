package com.developmentontheedge.be5.server.util

import com.developmentontheedge.be5.operation.util.OperationUtils
import org.junit.Test

import static org.junit.Assert.assertTrue

class ParseRequestUtilsTest
{
    @Test
    void selectedRowsTest()
    {
        assertTrue(Arrays.equals(["1", "2", "3"] as String[], OperationUtils.selectedRows("1,2,3")))

        assertTrue(Arrays.equals(["1"] as String[], OperationUtils.selectedRows("1")))

        assertTrue(Arrays.equals([] as String[], OperationUtils.selectedRows("")))
    }

}
