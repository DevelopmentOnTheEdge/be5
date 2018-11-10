package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.model.Query;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EventManager implements MethodInterceptor
{
    public static final Logger log = Logger.getLogger(EventManager.class.getName());
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_OPERATION = "operation";
    public static final String ACTION_QUERY_BUILDER = "queryBuilder";
    public static final String ACTION_LOGGING = "logging";
    public static final String ACTION_PRINT = "print";
    public static final String ACTION_SERVLET = "servlet";
    public static final String ACTION_PROCESS = "process";
    public static final String ACTION_OTHER = "other";

    @Inject
    private Meta meta;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Object[] arguments = invocation.getArguments();
        Object proceed = invocation.proceed();
        long estimatedTime = System.currentTimeMillis() - startTime;

        String className = invocation.getMethod().getDeclaringClass().getSimpleName();
        String name = invocation.getMethod().getName();

        if (className.equals("DocumentGeneratorImpl") &&
            (name.equals("queryJsonApiFor") || name.equals("updateQueryJsonApi")))
        {
            String entityName = (String) arguments[0];
            String queryName = (String) arguments[1];
            Query query = meta.getQuery(entityName, queryName);
            Map<String, Object> parameters = (Map<String, Object>) arguments[2];
            queryCompleted(query, parameters, estimatedTime);
        }
        return proceed;
    }

//    public void operationStarted( int pageID, String login, String entity, String title, OperationInfo opInfo )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.operationStarted( pageID, login, entity, title, opInfo );
//        }
//    }
//
//    public void operationCompleted( int pageID, OperationInfo opInfo )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//                listener.operationCompleted( pageID, opInfo );
//        }
//    }
//
//    public void operationDenied( int pageID, OperationInfo opInfo, String exc )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.operationDenied( pageID, opInfo, exc );
//        }
//    }

    public void queryCompleted(Query query, Map<String, Object> parameters, long estimatedTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryCompleted(query, parameters, estimatedTime);
        }
    }

    public void queryError(Query query, Map<String, Object> parameters, long estimatedTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryError(query, parameters, estimatedTime);
        }
    }
//
//    public void servletStarted( ServletInfo si ) //, ...
//
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//
//                listener.servletStarted( si );
//
//        }
//    }
//
//    public void servletCompleted( ServletInfo si )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//
//                listener.servletCompleted( si );
//
//        }
//    }
//
//    public void servletDenied( ServletInfo si, String reason )
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.servletDenied( si, reason );
//        }
//    }

    ///////////////////////////////////////////////////////////////////
    // methods for long processes and daemons
    //

//    static public void processStateChanged(ProcessInfo pi)
//    {
//        for( Be5EventLogger listener : listeners )
//        {
//            listener.processStateChanged(pi);
//        }
//    }

    private final List<Be5EventLogger> listeners = new ArrayList<>();

    public void addListener(Be5EventLogger listener)
    {
        listeners.add(listener);
    }

    public void removeListener(Be5EventLogger listener)
    {
        listeners.remove(listener);
    }

}
