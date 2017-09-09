package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilderFunctions
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.assertEquals


class TestAutocompleteFunctions extends Be5ProjectTest
{
    @Test
    void test()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        def gBuilder = new DynamicPropertyGBuilderFunctions()

        dps.add gBuilder.of{
            name                  "reasonMulti"
            displayName           "Множественный выбор"
            tagList               ([["fired", "Уволен"], ["other", "Иная причина"]] as String[][])
            value                 "fired"
        }

        assertEquals("{'values':{'reasonMulti':'fired'}," +
                "'meta':{'/reasonMulti':{" +
                "'displayName':'Множественный выбор'," +
                "'tagList':[['fired','Уволен'],['other','Иная причина']]}}," +
                "'order':['/reasonMulti']}",
                oneQuotes(JsonFactory.dps(dps).toString()))
    }

}
