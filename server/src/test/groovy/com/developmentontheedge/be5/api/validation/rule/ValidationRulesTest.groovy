package com.developmentontheedge.be5.api.validation.rule

import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Test

import static org.junit.Assert.*
import static com.developmentontheedge.be5.api.validation.rule.ValidationRules.*
import static com.developmentontheedge.be5.api.validation.rule.SimpleRule.*


class ValidationRulesTest extends Be5ProjectTest
{
    @Test
    void simpleRulesTest() throws Exception
    {
        def test = simpleRule(digits)
        assertEquals("{'attr':'digits','type':'simpleRule'}", oneQuotes(test.toString()))

        assertEquals("{'attr':'email','type':'simpleRule'}",
                oneQuotes(simpleRule(email).toString()))

        assertEquals("{'attr':'number','type':'simpleRule'}",
                oneQuotes(simpleRule(number).toString()))
    }

    @Test
    void rangeTest() throws Exception
    {
        def test = range(0, 10)
        assertEquals("{'attr':[0,10],'type':'range'}", oneQuotes(test.toString()))
    }

    @Test
    void uniqueTest() throws Exception
    {
        def test = unique("users")
        assertEquals("{'attr':{'tableName':'users'},'type':'unique'}", oneQuotes(test.toString()))
    }

}