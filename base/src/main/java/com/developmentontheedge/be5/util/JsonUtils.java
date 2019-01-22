package com.developmentontheedge.be5.util;

import com.developmentontheedge.beans.json.JsonFactory;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils
{
    public static Map<String, Object> getMapFromJson(String json)
    {
        if (json != null && !json.isEmpty())
        {
            return JsonFactory.jsonb.fromJson(json, new HashMap<String, Object>()
                    {
                    }.getClass().getGenericSuperclass());
        }
        else
        {
            return new HashMap<>();
        }
    }
}
