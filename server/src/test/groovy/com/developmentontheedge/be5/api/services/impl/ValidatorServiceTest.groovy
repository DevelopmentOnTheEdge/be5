package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.validation.Validator
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.DynamicProperty
import org.junit.Test


class ValidatorServiceTest extends AbstractProjectTest
{
    @Inject Validator validator

    @Test
    void name()
    {
        //parameters._account.attributes[ Validation.RULES_ATTR ] = [ "number": "Please enter only digits." ]
        DynamicProperty property = new DynamicProperty("name", "Name", String.class, 423423)
        property << [ VALIDATION_RULES: [ "number": "Please enter only digits." ] ]


        //validator.checkErrorAndCast(property)
    }
}
