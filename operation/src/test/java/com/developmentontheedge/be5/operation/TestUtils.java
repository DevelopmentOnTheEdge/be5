package com.developmentontheedge.be5.operation;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

public class TestUtils
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    protected static String oneQuotes(Object s)
    {
        return s.toString().replace("\"", "'");
    }

    protected static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

}
