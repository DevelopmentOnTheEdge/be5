package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static com.developmentontheedge.be5.api.validation.rule.BaseRule.digits
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.baseRule
import static org.junit.Assert.assertArrayEquals

class ValidatorServiceValueInTagsTest extends Be5ProjectTest
{
    @Inject Validator validator
    DynamicPropertySet dps

    @Before
    void initDps(){
        dps = new DynamicPropertySetSupport()
    }

    @Test
    void checkValueInTags()
    {
        add(dps) {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = "2"
        }
        validator.checkErrorAndCast(dps)
    }

    @Test
    void checkValueInTagsLong()
    {
        add(dps) {
            name          = "test"
            TYPE          = Long
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = 2L
        }
        validator.checkErrorAndCast(dps)
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsError()
    {
        add(dps) {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = "3"
        }

        validator.checkErrorAndCast(dps)
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleErrorStringValue()
    {
        add(dps) {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = ["1","3"] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        validator.checkErrorAndCast(dps)
    }

    @Test(expected = IllegalArgumentException.class)
    void checkValueInTagsMultipleError()
    {
        add(dps) {
            name          = "test"
            TAG_LIST_ATTR = [["1","1"],["2","2"]] as String[][]
            value         = [1,3] as Object[]
            MULTIPLE_SELECTION_LIST = true
        }

        validator.checkErrorAndCast(dps)
    }
}
