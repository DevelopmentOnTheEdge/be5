package com.developmentontheedge.be5.components.impl.model;

import java.util.regex.Pattern;

import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.Action;
import com.developmentontheedge.be5.util.HashLink;
import com.developmentontheedge.be5.util.HashLinks;
import com.developmentontheedge.be5.util.HashUrl;

final public class Queries 
{
    
    public static Action toAction(Query query) 
    {
        if (isClientSide(query))
        {
            return Action.call(asCliendSide(query).toHashUrl());
        }
        else if (isExternalRef(query))
        {
            return Action.open(query.getQuery());
        }
        else if (isViewBlob(query))
        {
            return Action.open(new HashUrl("api", "download", query.getEntity().getName(), query.getName()).toString());
        }
        else if (isAction(query))
        {
            return Action.call(query.getQuery());
        }
        else if (isStaticPage(query))
        {
            return Action.call(new HashUrl("static", query.getQuery()));
        }
        else
        {
            //LegacyUrlParser parser = legacyQueriesService.createParser(query.getQuery());

            if( query.getType() == QueryType.STATIC )
            {
                return Action.call(new HashUrl("servlet").named("path", query.getQuery()));
//                if( parser.isLegacy() )
//                {
//                    if (parser.isForm())
//                    {
//                        return Action.call(new HashUrl("form", parser.getEntityName(), parser.getQueryName(), parser.getOperationName()));
//                    }
//                    // continue
//                }
//                else
//                {
//                    return Action.call(new HashUrl("servlet").named("path", query.getQuery()));
//                }
            }

            return Action.call(new HashUrl("table", query.getEntity().getName(), query.getName()));
        }
    }

    public static Action toAction(String query, Operation operation) 
    {
        String entityName = operation.getEntity().getName();
        HashUrl hashUrl = new HashUrl("form", entityName, query, operation.getName());
        
        return Action.call(hashUrl);
    }
    
    // TODO move me to Query
    public static boolean isStaticPage(Query query) 
    {
        return query.getType() == QueryType.STATIC && query.getQuery().endsWith(".be");
    }
    
    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\w+$");
    
    private Queries() 
    {
        throw new IllegalStateException("Must not be instantiated");
    }
    
    private static boolean isClientSide(Query query) 
    {
        return query.getType() == QueryType.STATIC && HashLinks.isIn(query.getQuery());
    }
    
    private static HashLink asCliendSide(Query query) 
    {
        return HashLink.parse(query.getQuery());
    }

    private static boolean isExternalRef(Query query) 
    {
        return query.getType() == QueryType.STATIC && (query.getQuery().startsWith("http://") || query.getQuery().startsWith("https://"));
    }
    
    private static boolean isAction(Query query) 
    {
        return query.getType() == QueryType.STATIC && ACTION_PATTERN.matcher(query.getQuery()).matches();
    }
    
    private static boolean isViewBlob(Query query) 
    {
        return query.getType() == QueryType.STATIC && (query.getQuery() != null && query.getQuery().startsWith("viewBlob?")); 
    }
    
}
