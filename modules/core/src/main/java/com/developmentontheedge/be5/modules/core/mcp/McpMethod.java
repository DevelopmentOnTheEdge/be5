package com.developmentontheedge.be5.modules.core.mcp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class McpMethod
{
    private static final Set<String> AUTH_REQUIRED_METHODS = new HashSet<>(Arrays.asList(
            "tools/call", "resources/read"
    ));

    public static boolean requiresAuthentication(String method)
    {
        return method != null && AUTH_REQUIRED_METHODS.contains(method);
    }
}