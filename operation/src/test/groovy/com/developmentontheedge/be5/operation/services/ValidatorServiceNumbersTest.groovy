package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.exceptions.Be5Exception
import com.developmentontheedge.be5.groovy.GDynamicPropertySetSupport
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import com.developmentontheedge.be5.operation.validation.Validator
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals

class ValidatorServiceNumbersTest extends OperationsSqlMockProjectTest
{
    @Inject
    Validator validator
    GDynamicPropertySetSupport dps

    @Before
    void initDps()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST)
        dps = new GDynamicPropertySetSupport()
    }

    @Test(expected = Be5Exception.class)
    void Long1()
    {
        dps.add("test") {
            TYPE = Long
            value = '10000000000000000000000'
        }

        checkMessage("Здесь должно быть целое число. <= 9223372036854775807")
    }

    @Test(expected = Be5Exception.class)
    void Long2()
    {
        dps.add("test") {
            TYPE = Long
            value = '-10000000000000000000000'
        }

        checkMessage("Здесь должно быть целое число. >= -9223372036854775808")
    }

    @Test(expected = Be5Exception.class)
    void int1()
    {
        dps.add("test") {
            TYPE = Integer
            value = '3000000000'
        }

        checkMessage("Здесь должно быть целое число. <= 2147483647")
    }

    @Test(expected = Be5Exception.class)
    void int2()
    {
        dps.add("test") {
            TYPE = Integer
            value = '-3000000000'
        }

        checkMessage("Здесь должно быть целое число. >= -2147483648")
    }

    @Test(expected = Be5Exception.class)
    void short1()
    {
        dps.add("test") {
            TYPE = Short
            value = '100000'
        }

        checkMessage("Здесь должно быть целое число. <= 32767")
    }

    @Test(expected = Be5Exception.class)
    void short2()
    {
        dps.add("test") {
            TYPE = Short
            value = '-100000'
        }

        checkMessage("Здесь должно быть целое число. >= -32768")
    }

    @Test(expected = Be5Exception.class)
    void short3WithDot()
    {
        dps.add("test") {
            TYPE = Short
            value = '100.000'
        }

        checkMessage("Здесь должно быть целое число.")
    }

    @Test(expected = Be5Exception.class)
    void short3WithСomma()
    {
        dps.add("test") {
            TYPE = Short
            value = '100,000'
        }

        checkMessage("Здесь должно быть целое число.")
    }

    void checkMessage(String msg)
    {
        try {
            validator.checkAndThrowExceptionIsError(dps)
        } catch (RuntimeException e) {
            assertEquals("error", JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('status'))
            assertEquals(msg,
                    JsonFactory.dpsMeta(dps).getJsonObject("/test").getString('message'))
            throw Be5Exception.internal(e)
        }
    }
}
