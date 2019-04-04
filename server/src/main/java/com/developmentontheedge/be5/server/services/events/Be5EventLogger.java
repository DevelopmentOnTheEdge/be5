package com.developmentontheedge.be5.server.services.events;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.Operation;

import java.util.Map;

public interface Be5EventLogger
{
    void operationCompleted(Operation operation, Map<String, Object> values,
                            long startTime, long endTime);

    void operationError(Operation operation, Map<String, Object> values,
                        long startTime, long endTime, String exception);

    void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime);

    void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception);

    void servletCompleted(String servletName, String requestUri, Map<String, ?> params, long startTime, long endTime);

    void servletError(String servletName, String requestUri, Map<String, ?> params, long startTime, long endTime,
                      String exception);

    void logCompleted(String className, String methodName, Map<String, ?> params, long startTime, long endTime);

    void logException(String className, String methodName, Map<String, ?> params, long startTime, long endTime,
                      String exception);
}
