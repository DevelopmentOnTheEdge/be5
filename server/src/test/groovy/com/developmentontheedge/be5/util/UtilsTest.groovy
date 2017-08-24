package com.developmentontheedge.be5.util;

import org.junit.Test

import static org.junit.Assert.*

class UtilsTest
{
    @Test
    void changeType() throws Exception
    {
        assertEquals 3L, Utils.changeType("3", Long)
        assertEquals 3, Utils.changeType("3", Integer)
    }

    @Test
    void changeTypeArray() throws Exception
    {
        String[] stringArray = ["1", "2","3"] as String[]

        assertArrayEquals( [1L, 2L, 3L] as Long[], Utils.changeType(stringArray, Long[]))
        assertArrayEquals( [1, 2, 3] as Integer[], Utils.changeType(stringArray, Integer[]))
    }


}