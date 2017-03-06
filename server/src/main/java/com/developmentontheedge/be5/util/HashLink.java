package com.developmentontheedge.be5.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class HashLink {
    
    public static HashLink parse(String code) {
        try
        {
            JsonObject parsed = new JsonParser().parse(code).getAsJsonObject();
            String action = parsed.get("action").getAsString();
            Map<String, String> namedArgs = getNamedArgs(parsed);
            List<String> positionalArgs = getPositionalArgs(parsed);
            
            return new HashLink(action, positionalArgs, namedArgs);
        }
        catch (IllegalStateException | ClassCastException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (JsonParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private static Map<String, String> getNamedArgs(JsonObject action)
    {
        if (action.has("args") && action.get("args").isJsonObject())
        {
            return toNamedArgs(action.get("args").getAsJsonObject());
        }
        
        if (action.has("namedArgs"))
        {
            return toNamedArgs(action.get("namedArgs").getAsJsonObject());
        }
        
        return Collections.emptyMap();
    }

    private static List<String> getPositionalArgs(JsonObject action)
    {
        if (action.has("args") && action.get("args").isJsonArray())
        {
            return toPositionalArgs(action.get("args").getAsJsonArray());
        }
        
        if (action.has("positionalArgs"))
        {
            return toPositionalArgs(action.get("positionalArgs").getAsJsonArray());
        }
        
        return Collections.emptyList();
    }
    
    private static Map<String, String> toNamedArgs(JsonObject namedArgsJson)
    {
        return EntryStream.of(namedArgsJson.entrySet().stream()).mapValues(JsonElement::getAsString).toMap();
    }
    
    private static List<String> toPositionalArgs(JsonArray positionalArgsJson)
    {
        return StreamEx.of(positionalArgsJson.spliterator()).map(JsonElement::getAsString).toList();
    }

    public final String component;
    public final String[] positionalArgs;
    public final Map<String, String> namedArgs;
    
    private HashLink(String component, List<String> positionalArgs, Map<String, String> namedArgs) {
        this.component = component;
        this.positionalArgs = positionalArgs.toArray(new String[0]);
        this.namedArgs = namedArgs;
    }
    
    public HashUrl toHashUrl()
    {
        return new HashUrl(component).positional(positionalArgs).named(namedArgs);
    }
    
}