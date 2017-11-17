package com.developmentontheedge.be5.util

import com.developmentontheedge.be5.components.Form;
import org.junit.Test;

import static org.junit.Assert.*;

class JsonUtilsTest
{
    @Test
    void selectedRowsTest()
    {
        assertTrue( Arrays.equals(["1","2","3"] as String[], JsonUtils.selectedRows("1,2,3")))

        assertTrue( Arrays.equals(["1"] as String[], JsonUtils.selectedRows("1")))

        assertTrue( Arrays.equals([] as String[], JsonUtils.selectedRows("")))
    }

}