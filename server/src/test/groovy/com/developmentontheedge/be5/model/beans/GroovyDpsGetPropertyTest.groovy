package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.test.ServerBe5ProjectTest
import org.junit.Test

import static org.junit.Assert.assertEquals


class GroovyDpsGetPropertyTest extends ServerBe5ProjectTest
{
    private GDynamicPropertySetSupport dps

    @Test
    void test()
    {
        dps = new GDynamicPropertySetSupport()

        dps.add("input2") {
            value = "value2"
        }

        assertEquals("value2", dps["input2"].getValue() )

        assertEquals("value2", dps.input2.getValue() )// not TypeChecked

        assertEquals("value2", dps.$input2 )// not TypeChecked
    }

}
