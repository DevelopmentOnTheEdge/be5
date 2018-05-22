package com.developmentontheedge.be5.util;

import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class DpsUtilsTest
{
    @Test
    public void setValueIfOneTag()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicPropertyBuilder("test", String.class)
                .tags(new String[][]{{"one", "one"}}).get());

        DpsUtils.setValueIfOneTag(dps, ImmutableList.of("test"));

        assertEquals("one", dps.getValue("test"));
    }

    @Test
    public void setValueIfOneTag_canBeNull()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicPropertyBuilder("test", String.class)
                .tags(new String[][]{{"one", "one"}})
                .nullable()
                .get());

        DpsUtils.setValueIfOneTag(dps, ImmutableList.of("test"));

        assertNull(dps.getValue("test"));
    }
}