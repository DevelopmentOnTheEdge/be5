package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.util.HashUrl;

public class Action
{
    
    public final String name;
    public final String arg;

    public Action(String name, String arg)
    {
        this.name = name;
        this.arg = arg;
    }

    /**
     * Generates an action that should be interpreted as client-side action via a hash URL. 
     */
    public static Action call(HashUrl hashUrl)
    {
        return Action.create("call", hashUrl.toString());
    }

    /**
     * Generates an action that should be interpreted as client-side action via a hash URL. 
     */
    public static Action call(String link)
    {
        return Action.create("call", link);
    }

    /**
     * Generates an action that should be interpreted as an external link.
     */
    public static Action open(String link)
    {
        return Action.create("open", link);
    }

    private static Action create(String action, String arg)
    {
        return new Action(action, arg);
    }

}
