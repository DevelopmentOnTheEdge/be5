package com.developmentontheedge.be5.metadata.freemarker;

import static org.junit.Assert.*;

import org.junit.Test;

public class FreemarkerUtilsTest
{
    @Test
    public void testEscape()
    {
        assertEquals("test", FreemarkerUtils.escapeFreemarker( "test" ));
        assertEquals("<#noparse><#if>test</#noparse>", FreemarkerUtils.escapeFreemarker( "<#if>test" ));
        assertEquals("<#noparse><@macro>test</#noparse>", FreemarkerUtils.escapeFreemarker( "<@macro>test" ));
        assertEquals("<#noparse>${test}</#noparse>", FreemarkerUtils.escapeFreemarker( "${test}" ));
    }
}
