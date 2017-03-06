package com.developmentontheedge.be5.util;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class SqlStatements
{

    public static final String NULL = "NULL";

    public static boolean isTableName(String string)
    {
        requireNonNull(string);
        return isIdentifier(string);
    }
    
    public static boolean isFieldName(String string)
    {
        requireNonNull(string);
        
        if (string.indexOf('.') != -1)
        {
            List<String> splitted = Splitter.on('.').splitToList(string);
            return splitted.size() == 2 && isTableName(splitted.get(0)) && isIdentifier(splitted.get(1));
        }
        
        return isIdentifier(string);
    }
    
    private static boolean isIdentifier(String identifier)
    {
        if (identifier.isEmpty())
        {
            return false;
        }
        
        return CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.is('_')).matchesAllOf(identifier);
    }

}
