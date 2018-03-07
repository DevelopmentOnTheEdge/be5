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
    void baseRulesTest()
    {
        def test = baseRule(digits)
        assertEquals("{'attr':'digits','type':'baseRule'}", oneQuotes(test.toString()))

        assertEquals("{'attr':'email','type':'baseRule'}",
                oneQuotes(baseRule(email).toString()))

        assertEquals("{'attr':'digits','type':'baseRule'}",
                oneQuotes(baseRule(digits).toString()))
    }

    @Test
    void rangeTest()
    {
        def test = range(0, 10)
        assertEquals("{'attr':{'from':0,'to':10},'type':'range'}", oneQuotes(test.toString()))

        test = range(0, 0.5)
        assertEquals("{'attr':{'from':0.0,'to':0.5},'type':'range'}", oneQuotes(test.toString()))
    }

    @Test
    void stepTest()
    {
        def test = step(10)
        assertEquals("{'attr':10,'type':'step'}", oneQuotes(test.toString()))

        test = step(0.5)
        assertEquals("{'attr':0.5,'type':'step'}", oneQuotes(test.toString()))
    }

    @Test
    void patternTest()
    {
        def test = pattern('[A-Za-zА-Яа-яЁё]')
        assertEquals("{'attr':'[A-Za-zА-Яа-яЁё]','type':'pattern'}", oneQuotes(test.toString()))
    }

    @Test
    void uniqueTest()
    {
        def test = unique("users")
        assertEquals("{'attr':{'tableName':'users'},'type':'unique'}", oneQuotes(test.toString()))
    }

    @Test
    void manyTest()
    {
        def list = [baseRule(digits), unique("users")]
        assertEquals("[" +
                "{'attr':'digits','type':'baseRule'}, " +
                "{'attr':{'tableName':'users'},'type':'unique'}" +
            "]", oneQuotes(list.toString()))
    }

    @Test
    void objectInDps()
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
    void listInDps()
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