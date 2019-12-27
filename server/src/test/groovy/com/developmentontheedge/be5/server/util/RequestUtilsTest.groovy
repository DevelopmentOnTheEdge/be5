package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.test.BaseTestUtils
import org.junit.Test

import static org.junit.Assert.assertEquals

public class RequestUtilsTest extends BaseTestUtils
{

    @Test
    void getFileNames()
    {
        assertEquals("ABCZabcz09-", RequestUtils.parseFileName("ABCZabcz09-"))
        assertEquals("%D0%A2%D0%B5%D1%81%D1%82%2012%20%23%24.xls", RequestUtils.parseFileName("Тест 12 #\$.xls"))
    }

}
