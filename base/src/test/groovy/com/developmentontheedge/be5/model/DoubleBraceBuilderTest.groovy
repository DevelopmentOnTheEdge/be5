package com.developmentontheedge.be5.model

import com.developmentontheedge.beans.DPBuilder
import com.developmentontheedge.beans.DynamicPropertySetSupport
import groovy.transform.TypeChecked
import org.junit.Test

import static com.developmentontheedge.beans.BeanInfoConstants.GROUP_ID
import static org.junit.Assert.assertEquals

@TypeChecked
class DoubleBraceBuilderTest
{
    @Test
    void test()
    {
        DynamicPropertySetSupport params = new DynamicPropertySetSupport();
        params.add(new DPBuilder("name") {{
            readonly = true
            tags = [][] as String[][]
            multiple = true
            value = "test"
            attr(GROUP_ID, 0)
        }}.build())

        assertEquals("test", params.getValue("name"))
        assertEquals(true, params.getProperty("name").isReadOnly())
        assertEquals(0, params.getProperty("name").getAttribute(GROUP_ID))

        new DPBuilder(params.getProperty("name")) {{
            value = null
        }}.build()

        assertEquals(null, params.getValue("name"))
    }
}
