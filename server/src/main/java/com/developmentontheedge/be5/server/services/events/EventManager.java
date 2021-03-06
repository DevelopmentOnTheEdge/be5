package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.Utils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.operation.OperationConstants.RELOAD_CONTROL_NAME;

public class EventManager implements MethodInterceptor
{
    public static final Logger log = Logger.getLogger(EventManager.class.getName());
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_OPERATION = "operation";
    public static final String ACTION_SERVLET = "servlet";
//    public static final String ACTION_QUERY_BUILDER = "queryBuilder";
    public static final String ACTION_LOGGING = "logging";
//    public static final String ACTION_PRINT = "print";

//    public static final String ACTION_PROCESS = "process";
//    public static final String ACTION_OTHER = "other";

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Object[] arguments = invocation.getArguments();
        String className = invocation.getMethod().getDeclaringClass().getSimpleName();

        if (className.equals("DocumentGeneratorImpl"))
        {
            return logQuery(invocation, startTime, arguments);
        }
        if (className.equals("FormGeneratorImpl"))
        {
            return logOperation(invocation, startTime, arguments);
        }

        return logLogging(invocation, startTime, arguments, className);
    }

    private Object logLogging(MethodInvocation invocation, long startTime, Object[] arguments, String className) throws Throwable
    {
        String methodName = invocation.getMethod().getName();
        try
        {
            Object proceedResult = invocation.proceed();
            logCompleted(className, methodName, getParamsByID(arguments), startTime, System.currentTimeMillis());
            return proceedResult;
        }
        catch (Throwable e)
        {
            logException(className, methodName, getParamsByID(arguments), startTime, System.currentTimeMillis(),
                    e.getMessage());
            throw e;
        }
    }

    private Map<String, ?> getParamsByID(Object[] arguments)
    {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < arguments.length; i++)
        {
            map.put("param" + (i + 1), arguments[i].toString());
        }
        return map;
    }

    private Object logQuery(MethodInvocation invocation, long startTime, Object[] arguments) throws Throwable
    {
        Query query = (Query) arguments[0];
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) arguments[1];
        try
        {
            Object proceed = invocation.proceed();
            queryCompleted(query, parameters, startTime, System.currentTimeMillis());
            return proceed;
        }
        catch (Throwable e)
        {
            queryError(query, parameters, startTime, System.currentTimeMillis(), e.getMessage());
            throw e;
        }
    }

    private Object logOperation(MethodInvocation invocation, long startTime, Object[] arguments) throws Throwable
    {
        Operation operation = (Operation) arguments[0];
        @SuppressWarnings("unchecked")
        Map<String, Object> values = (Map<String, Object>) arguments[1];

        Object proceed = invocation.proceed();
        if (operation.getStatus() == OperationStatus.ERROR)
        {
            operationError(operation, values, startTime, System.currentTimeMillis(), operation.getResult().getMessage());
        }
        else if (!values.containsKey(RELOAD_CONTROL_NAME))
        {
            operationCompleted(operation, values, startTime, System.currentTimeMillis());
        }
        return proceed;
    }

    private void operationCompleted(Operation operation, Map<String, Object> values,
                                    long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.operationCompleted(operation, values, startTime, endTime);
        }
    }

    private void operationError(Operation operation, Map<String, Object> values,
                                long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.operationError(operation, values, startTime, endTime, notEmptyException(exception));
        }
    }

    private void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryCompleted(query, parameters, startTime, endTime);
        }
    }

    private void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.queryError(query, parameters, startTime, endTime, notEmptyException(exception));
        }
    }

    public void servletCompleted(String servletName, String requestUri, Map<String, ?> params,
                                 long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.servletCompleted(servletName, requestUri, params, startTime, endTime);
        }
    }

    public void servletError(String servletName, String requestUri, Map<String, ?> params,
                             long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.servletError(servletName, requestUri, params, startTime, endTime, notEmptyException(exception));
        }
    }

    public void logCompleted(String className, String methodName, Map<String, ?> params,
                                 long startTime, long endTime)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.logCompleted(className, methodName, params, startTime, endTime);
        }
    }

    public void logException(String className, String methodName, Map<String, ?> params,
                             long startTime, long endTime, String exception)
    {
        for (Be5EventLogger listener : listeners)
        {
            listener.logException(className, methodName, params, startTime, endTime, notEmptyException(exception));
        }
    }

    private String notEmptyException(String exception)
    {
        return !Utils.isEmpty(exception) ? exception : "Exception (empty message)";
    }

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
}
