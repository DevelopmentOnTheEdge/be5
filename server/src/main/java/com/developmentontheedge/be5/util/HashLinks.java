package com.developmentontheedge.be5.util;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class HashLinks
{

    public static boolean isIn(String code)
    {
        try
        {
            return new JsonParser().parse(code).getAsJsonObject().has("action");
        }
        catch (IllegalStateException | JsonParseException e)
        {
            return false;
        }
    }

}
