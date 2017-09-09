package com.developmentontheedge.be5.model.beans

import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import groovy.transform.TypeChecked
import org.junit.Test

import static com.developmentontheedge.be5.model.beans.DynamicPropertyGBuilder.*
import static org.junit.Assert.assertEquals

@TypeChecked
class TestAutocomplete extends Be5ProjectTest
{
    @Test
    void test()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        add (dps) {
            name          = "reasonMulti"
            DISPLAY_NAME  = "Множественный выбор"
            TAG_LIST_ATTR = [["fired", "Уволен"], ["other", "Иная причина"]] as String[][]
            value         = "fired"
        }

        assertEquals("{'values':{'reasonMulti':'fired'}," +
                "'meta':{'/reasonMulti':{" +
                    "'displayName':'Множественный выбор'," +
                    "'tagList':[['fired','Уволен'],['other','Иная причина']]}}," +
                "'order':['/reasonMulti']}",
                oneQuotes(JsonFactory.dps(dps).toString()))
    }

}
