package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public interface MockUtils
{
    default Request getMockRequest(String requestUri)
    {
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    default Request getSpyMockRequest(String requestUri)
    {
        return getSpyMockRequest(requestUri, new HashMap<>(), new HashMap<>());
    }

    default Request getSpyMockRequest(String requestUri, Map<String, String> parameters)
    {
        return getSpyMockRequest(requestUri, parameters, new HashMap<>());
    }

    default Request getSpyMockRequest(String requestUri, Map<String, String> parameters, Map<String, Object> sessionValues)
    {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        when(request.getRequestUri()).thenReturn(requestUri);

        for (Map.Entry<String, Object> entry: sessionValues.entrySet())
        {
            when(request.getAttribute(entry.getKey())).thenReturn(entry.getValue());
        }

        return request;
    }

    default Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values, Map<String, Object> sessionValues)
    {
        return getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, entity,
                RestApiConstants.QUERY, query,
                RestApiConstants.OPERATION, operation,
                RestApiConstants.SELECTED_ROWS, selectedRows,
                RestApiConstants.VALUES, values),
                sessionValues
        );
    }

    default Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values)
    {
        return getSpyMockRecForOp(entity, query, operation, selectedRows, values, new HashMap<>());
    }
}
