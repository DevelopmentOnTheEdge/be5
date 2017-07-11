package com.developmentontheedge.be5.util;

import java.util.List;
import java.util.regex.Pattern;

import one.util.streamex.StreamEx;

/**
 * URL parsers.
 * 
 * @author asko
 */
@Deprecated
final public class Urls
{
//    private Urls()
//    {
//        // not intended to be instantiated
//    }
//
//    /**
//     *  Extracts a path from the URL part starting with path and decodes its parts.
//     *  URL: scheme:[//[user:password@]host[:port]][/]path[?query][#fragment]
//     */
//    public static List<String> extractPath(final String pathAndQueryAndFragment)
//    {
//        int indexOfQuery = pathAndQueryAndFragment.indexOf('?');
//        int indexOfFragment = pathAndQueryAndFragment.indexOf('#');
//
//        if (indexOfQuery == -1) { indexOfQuery = pathAndQueryAndFragment.length(); }
//        if (indexOfFragment == -1) { indexOfFragment = pathAndQueryAndFragment.length(); }
//
//        return parsePath(pathAndQueryAndFragment.substring(0, Math.min(indexOfQuery, indexOfFragment)));
//    }
//
//    private static List<String> parsePath(final String path)
//    {
//        return StreamEx.split(path, Pattern.quote("/")).map(MoreStrings::decodeUrl).toList();
//    }
//
}
