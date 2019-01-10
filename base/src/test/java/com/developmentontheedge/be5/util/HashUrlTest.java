package com.developmentontheedge.be5.util;

import org.junit.Test;

import java.util.Collections;

import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;
import static org.junit.Assert.*;


public class HashUrlTest
{
    @Test
    public void test()
    {
        assertEquals("table/entity/query/name=value", new HashUrl(TABLE_ACTION,
                "entity", "query")
                .named(Collections.singletonMap("name", "value")).toString());
    }

    @Test
    public void test2()
    {
        assertEquals(new HashUrl(TABLE_ACTION, "entity", "query").named(Collections.singletonMap("name", "value")),
                new HashUrl(TABLE_ACTION, "entity").positional("query").named("name", "value"));
    }
}
