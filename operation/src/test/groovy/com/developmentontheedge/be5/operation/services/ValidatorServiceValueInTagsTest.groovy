package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.groovy.GDynamicPropertySetSupport
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.be5.operation.validation.Validator
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

class ValidatorServiceValueInTagsTest extends OperationsSqlMockProjectTest
{
    @Inject
    Validator validator
    GDynamicPropertySetSupport dps

    @Before
    void initDps()
    {
        dps = new GDynamicPropertySetSupport()
    }

    @Test
    void checkValueInTags()
    {
        dps.add {
            name = "test"
            TAG_LIST_ATTR = [["1", "1"], ["2", "2"]] as String[][]
            value = "2"
        }
        validator.checkAndThrowExceptionIsError(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test
    void checkValueInTagsLong()
    {
        dps.add {
            name = "test"
            TYPE = Long
            TAG_LIST_ATTR = [["1", "1"], ["2", "2"]] as String[][]
            value = 2L
        }
        validator.checkAndThrowExceptionIsError(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsError()
    {
        dps.add {
            name = "test"
            TAG_LIST_ATTR = [["1", "1"], ["2", "2"]] as String[][]
            value = "3"
        }

        try {
            validator.checkAndThrowExceptionIsError(dps)
        } catch (RuntimeException e) {
            assertEquals("error", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('status'))
            assertEquals("Value is not contained in tags: 3", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('message'))
            throw e
        }

    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleErrorStringValue()
    {
        dps.add {
            name = "test"
            TAG_LIST_ATTR = [["1", "1"], ["2", "2"]] as String[][]
            value = ["1", "3"] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        validator.checkAndThrowExceptionIsError(dps)
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('status'))
        assertNull(JsonFactory.dpsMeta(dps).getJsonObject("/test").get('message'))
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleError()
    {
        dps.add {
            name = "test"
            TAG_LIST_ATTR = [["1", "1"], ["2", "2"]] as String[][]
            value = [1, 3] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        try {
            validator.checkAndThrowExceptionIsError(dps)
        } catch (RuntimeException e) {
            assertEquals("error", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('status'))
            assertEquals("Value is not contained in tags: 3", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('message'))
            throw e
        }
    }
}
