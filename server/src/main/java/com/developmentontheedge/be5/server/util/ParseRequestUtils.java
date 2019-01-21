package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.operation.util.OperationUtils;
import com.developmentontheedge.be5.server.model.Base64File;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParseRequestUtils
{
    private static Object checkAndParseBase64File(String value)
    {
        if (value.contains("\"type\":\"Base64File\""))
        {
            JsonParser parser = Json.createParser(new StringReader(value));
            parser.next();
            JsonObject jsonObject = parser.getObject();
            try
            {
                String data = getJsonStringValue(jsonObject.get("data"));
                String base64 = ";base64,";
                int base64Pos = data.indexOf(base64);
                String mimeTypes = data.substring("data:".length(), base64Pos);
                byte[] bytes = data.substring(base64Pos + base64.length(), data.length()).getBytes("UTF-8");
                byte[] decoded = Base64.getDecoder().decode(bytes);
                return new Base64File(getJsonStringValue(jsonObject.get("name")), decoded, mimeTypes);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return value;
        }
    }

    public static Map<String, Object> getFormValues(Map<String, String[]> parameters)
    {
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, String[]> param : parameters.entrySet())
        {
            if (param.getValue().length == 1)
            {
                values.put(param.getKey(), checkAndParseBase64File(param.getValue()[0]));
            }
            else
            {
                values.put(param.getKey(), param.getValue());
            }
        }
        return OperationUtils.replaceEmptyStringToNull(values);
    }

    public static Map<String, Object> getContextParams(String json)
    {
        if (json == null) return Collections.emptyMap();
        JsonParser parser = Json.createParser(new StringReader(json));
        if (!parser.hasNext()) return Collections.emptyMap();
        parser.next();
        return getContextParams(parser.getObject());
    }

    private static Map<String, Object> getContextParams(JsonObject jsonObject)
    {
        Map<String, Object> contextParams = new LinkedHashMap<>();
        for(Map.Entry<String, JsonValue> param : jsonObject.entrySet())
        {
            if (param.getValue() instanceof JsonArray)
            {
                JsonArray value = param.getValue().asJsonArray();
                String[] arrValues = new String[value.size()];
                for (int i = 0; i < value.size(); i++)
                {
                    arrValues[i] = getJsonStringValue(value.get(i));
                }
                contextParams.put(param.getKey(), arrValues);
            }
            else
            {
                contextParams.put(param.getKey(), getJsonStringValue(param.getValue()));
            }
        }
        return contextParams;
    }

    private static String getJsonStringValue(JsonValue value)
    {
        if (value instanceof JsonString)
        {
            return ((JsonString) value).getString();
        }
        else
        {
            return value.toString();
        }
    }
}
