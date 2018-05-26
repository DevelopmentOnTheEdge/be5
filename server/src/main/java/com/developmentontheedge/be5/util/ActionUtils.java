package com.developmentontheedge.be5.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.Action;


final public class ActionUtils
{
    public static Action toAction(Query query) 
    {
        if (isExternalRef(query))
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
            if( query.getType() == QueryType.STATIC )
            {
                //move to static LegacyUrlParser
                //mspReceiverCategories.redir
                if(query.getQuery().contains(".redir")){
                    String[] parts = query.getQuery().split(".redir");
                    Map<String, String> params = new HashMap<>();
                    if(parts.length>1)
                    {
                        String[] paramsVal = parts[1].replace("?", "").split("&");

                        for (String s : paramsVal)
                        {
                            String[] split = s.split("=");
                            params.put(split[0], split[1].replace("+", " "));
                        }
                    }
                    HashUrl hashUrl;
                    if(params.size() == 0)
                    {
                        hashUrl = new HashUrl("table", parts[0]);
                    }
                    else if(params.get("_on_") != null)
                    {
                        hashUrl = new HashUrl("form", parts[0], params.remove("_qn_"), params.remove("_on_"));
                    }
                    else //if(params.get("_qn_") != null)
                    {
                        hashUrl = new HashUrl("table", parts[0], params.remove("_qn_"));
                    }

                    return Action.call(hashUrl.named(params));
                }

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
    
    private ActionUtils()
    {
        throw new IllegalStateException("Must not be instantiated");
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
