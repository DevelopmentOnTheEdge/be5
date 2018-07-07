package com.developmentontheedge.be5.metadata.freemarker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FreemarkerUtilsTest
{
    @Test
    public void testEscape()
    {
        assertEquals("test", FreemarkerUtils.escapeFreemarker("test"));
        assertEquals("<#noparse><#if>test</#noparse>", FreemarkerUtils.escapeFreemarker("<#if>test"));
        assertEquals("<#noparse><@macro>test</#noparse>", FreemarkerUtils.escapeFreemarker("<@macro>test"));
        assertEquals("<#noparse>${test}</#noparse>", FreemarkerUtils.escapeFreemarker("${test}"));
    }
}
