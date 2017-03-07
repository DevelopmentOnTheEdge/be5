package com.developmentontheedge.be5.util;

import java.util.Map;

import one.util.streamex.StreamEx;

import com.google.common.collect.ImmutableMap;

public class QueryStrings
{
    /**
     * Returns an immutable map of parameters.
     */
    public static Map<String, String> extract(String url) {
        int indexOfQuestionMark = url.indexOf('?');
        
        if (indexOfQuestionMark == -1)
        {
            return ImmutableMap.of();
        }
        
        return parse(url.substring(indexOfQuestionMark + 1));
    }
    
    /**
     * Returns an immutable map of parameters.
     */
    public static Map<String, String> parse(String parametersStr) {
        return StreamEx.split( parametersStr, "&" ).map( str -> str.split( "=" ) )
                .mapToEntry( arr -> MoreStrings.decodeUrl(arr[0]), arr -> arr.length == 2 ? MoreStrings.decodeUrl(arr[1]) : "" )
                .toMap();
    }

}
