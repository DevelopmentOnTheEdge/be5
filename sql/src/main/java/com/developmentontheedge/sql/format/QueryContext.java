package com.developmentontheedge.sql.format;

import java.util.List;
import java.util.Map;

import one.util.streamex.StreamEx;

/**
 * Query execution context
 * Must be immutable object
 */
public interface QueryContext
{
    /**
     * @param name
     *            parameter name
     * @return parameter value by name or null if parameter absent
     */
    public List<String> getListParameter(String name);
    
    public String getParameter(String name);

    /**
     * @param name
     *            session variable name
     * @return session variable value or null if such variable does not exist
     */
    public String getSessionVariable(String name);

    public Map<String, String> asMap();
    
    public String resolveQuery(String entity, String name);

    /**
     * @return name of the current user
     */
    String getUserName();
    
    StreamEx<String> roles();
}
