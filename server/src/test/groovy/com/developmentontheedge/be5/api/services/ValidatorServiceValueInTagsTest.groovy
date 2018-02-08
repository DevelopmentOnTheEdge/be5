package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull


class ValidatorServiceValueInTagsTest extends Be5ProjectTest
{
    @Inject Validator validator
    GDynamicPropertySetSupport dps

    @Before
    void initDps(){
        dps = new GDynamicPropertySetSupport()
    }

    @Test
    void checkValueInTags()
    {
        dps.add {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = "2"
        }
        validator.checkErrorAndCast(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test
    void checkValueInTagsLong()
    {
        dps.add {
            name          = "test"
            TYPE          = Long
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = 2L
        }
        validator.checkErrorAndCast(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsError()
    {
        dps.add {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = "3"
        }

        try {
            validator.checkErrorAndCast(dps)
        }catch (RuntimeException e){
            assertEquals("error", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('status'))
            assertEquals("Value is not contained in tags", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('message'))
            throw e
        }

    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleErrorStringValue()
    {
        dps.add {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = ["1","3"] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        validator.checkErrorAndCast(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleError()
    {
        dps.add {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = [1,3] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        try {
            validator.checkErrorAndCast(dps)
        }catch (RuntimeException e){
            assertEquals("error", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('status'))
            assertEquals("Value is not contained in tags", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('message'))
            throw e
        }
    }
}
