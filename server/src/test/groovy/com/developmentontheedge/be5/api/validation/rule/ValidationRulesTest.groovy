package com.developmentontheedge.be5.api.validation.rule

import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*


class ValidationRulesTest extends Be5ProjectTest
{
    @Test
    void rangeTest()
    {
        def test = range(0, 10)
        assertEquals("{'attr':{'max':'10','min':'0'},'type':'range'}", oneQuotes(test.toString()))

        test = range(100, 999, 'enter 3 digits')
        assertEquals("{'attr':{'max':'999','min':'100'},'customMessage':'enter 3 digits','type':'range'}", oneQuotes(test.toString()))

        test = range(0, 0.5)
        assertEquals("{'attr':{'max':'0.5','min':'0.0'},'type':'range'}", oneQuotes(test.toString()))

        test = range(0, 0.5, 'text')
        assertEquals("{'attr':{'max':'0.5','min':'0.0'},'customMessage':'text','type':'range'}", oneQuotes(test.toString()))
    }

    @Test
    void stepTest()
    {
        def test = step(10)
        assertEquals("{'attr':'10','type':'step'}", oneQuotes(test.toString()))

        test = step(10, 'enter an integer')
        assertEquals("{'attr':'10','customMessage':'enter an integer','type':'step'}", oneQuotes(test.toString()))

        test = step(0.5)
        assertEquals("{'attr':'0.5','type':'step'}", oneQuotes(test.toString()))

        test = step(0.01, 'Must be monetary amount')
        assertEquals("{'attr':'0.01','customMessage':'Must be monetary amount','type':'step'}", oneQuotes(test.toString()))
    }

    @Test
    void patternTest()
    {
        def test = pattern('[A-Za-zА-Яа-яЁё]')
        assertEquals("{'attr':'[A-Za-zА-Яа-яЁё]','type':'pattern'}", oneQuotes(test.toString()))

        test = pattern('[A-Za-zА-Яа-яЁё]', 'Enter only en/ru letters')
        assertEquals("{'attr':'[A-Za-zА-Яа-яЁё]','customMessage':'Enter only en/ru letters','type':'pattern'}",
                oneQuotes(test.toString()))
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
        def list = [pattern('^[0-9]+$'), unique("users")]
        assertEquals("[" +
                "{'attr':'^[0-9]+\$','type':'pattern'}, " +
                "{'attr':{'tableName':'users'},'type':'unique'}" +
            "]", oneQuotes(list.toString()))
    }

    @Test
    void objectInDps()
    {
        def dps = new DynamicPropertySetSupport()

        dps << [
                name: "test",
                VALIDATION_RULES: pattern('^[0-9]+$')
        ]

        assertEquals("{'/test':{'displayName':'test','validationRules':{'attr':'^[0-9]+\$','type':'pattern'}}}"
                , oneQuotes(JsonFactory.beanMeta(dps).toString()))
    }

    @Test
    void listInDps()
    {
        def dps = new DynamicPropertySetSupport()

        dps << [
                name: "test",
                VALIDATION_RULES: [pattern('^[0-9]+$'), unique("users")]
        ]

        assertEquals("{'/test':{'displayName':'test'," +
                    "'validationRules':[" +
                        "{'attr':'^[0-9]+\$','type':'pattern'}," +
                        "{'attr':{'tableName':'users'},'type':'unique'}" +
                    "]" +
                "}}" , oneQuotes(JsonFactory.beanMeta(dps).toString()))
    }

}