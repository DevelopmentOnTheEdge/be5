package com.developmentontheedge.be5.api.validation.rule

import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*
import static com.developmentontheedge.be5.api.validation.rule.BaseRule.*


class ValidationRulesTest extends Be5ProjectTest
{
    @Test
    void baseRulesTest() throws Exception
    {
        def test = baseRule(digits)
        assertEquals("{'attr':'digits','type':'baseRule'}", oneQuotes(test.toString()))

        assertEquals("{'attr':'email','type':'baseRule'}",
                oneQuotes(baseRule(email).toString()))

        assertEquals("{'attr':'number','type':'baseRule'}",
                oneQuotes(baseRule(number).toString()))
    }

    @Test
    void rangeTest() throws Exception
    {
        def test = range(0, 10)
        assertEquals("{'attr':{'from':0,'to':10},'type':'range'}", oneQuotes(test.toString()))
    }

    @Test
    void uniqueTest() throws Exception
    {
        def test = unique("users")
        assertEquals("{'attr':{'tableName':'users'},'type':'unique'}", oneQuotes(test.toString()))
    }

    @Test
    void manyTest() throws Exception
    {
        def list = [baseRule(digits), unique("users")]
        assertEquals("[" +
                "{'attr':'digits','type':'baseRule'}, " +
                "{'attr':{'tableName':'users'},'type':'unique'}" +
            "]", oneQuotes(list.toString()))
    }

    @Test
    void objectInDps() throws Exception
    {
        def dps = new DynamicPropertySetSupport()

        dps << [
                name: "test",
                VALIDATION_RULES: baseRule(digits)
        ]

        assertEquals("{'/test':{'displayName':'test','validationRules':{'attr':'digits','type':'baseRule'}}}"
                , oneQuotes(JsonFactory.beanMeta(dps).toString()))
    }

    @Test
    void listInDps() throws Exception
    {
        def dps = new DynamicPropertySetSupport()

        dps << [
                name: "test",
                VALIDATION_RULES: [baseRule(digits), unique("users")]
        ]

        assertEquals("{'/test':{'displayName':'test'," +
                    "'validationRules':[" +
                        "{'attr':'digits','type':'baseRule'}," +
                        "{'attr':{'tableName':'users'},'type':'unique'}" +
                    "]" +
                "}}" , oneQuotes(JsonFactory.beanMeta(dps).toString()))
    }

}