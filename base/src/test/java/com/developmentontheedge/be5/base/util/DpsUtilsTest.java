package com.developmentontheedge.be5.base.util;

import com.developmentontheedge.beans.DynamicPropertyBuilder;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
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

    @Test
    public void toLinkedHashMap()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicPropertyBuilder("value", String.class).get());
        dps.add(new DynamicPropertyBuilder("test", String.class).value("foo").get());

        Map<String, Object> map = DpsUtils.toLinkedHashMap(dps);
        assertEquals("foo", map.get("test"));
        assertEquals(null, map.get("value"));
        assertArrayEquals(new String[]{"value", "test"}, map.keySet().toArray());
    }

    @Test
    public void setValues()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicPropertyBuilder("value", String.class).get());
        dps.add(new DynamicPropertyBuilder("test", String.class).get());

        DpsUtils.setValues(dps, ImmutableMap.of("value", "1", "test", "2"));

        assertEquals("1", dps.getValue("value"));
        assertEquals("2", dps.getValue("test"));
    }

    @Test
    public void setValuesFromDps()
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicPropertyBuilder("value", String.class).get());
        dps.add(new DynamicPropertyBuilder("test", String.class).get());

        DynamicPropertySetSupport dps2 = new DynamicPropertySetSupport();
        dps2.add(new DynamicPropertyBuilder("value", String.class).value("1").get());
        dps2.add(new DynamicPropertyBuilder("test", String.class).value("2").get());

        DpsUtils.setValues(dps, dps2);

        assertEquals("1", dps.getValue("value"));
        assertEquals("2", dps.getValue("test"));
    }
}