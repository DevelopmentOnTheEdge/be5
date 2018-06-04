package com.developmentontheedge.be5.base;

import com.developmentontheedge.be5.base.util.DpsUtils;
import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


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