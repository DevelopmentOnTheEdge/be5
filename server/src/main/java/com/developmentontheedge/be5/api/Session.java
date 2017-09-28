package com.developmentontheedge.be5.api;

/**
 * A high-level access to the session.
 * 
 * @author lan
 */
public interface Session
{
    /**
     * Returns the current session identifier.
     */
    String getSessionId();

    /**
     * Retrieves named attribute from the session
     * @param name an attribute name
     * @return an attribute value stored in the session
     */
    Object get(String name);

    /**
     * Stores named attribute into the session
     * @param name an attribute name
     * @param value an attribute value
     */
    void set(String name, Object value);

    //todo move to groovy meta
    default Object getAt(String name)
    {
        return get(name);
    }

    default void putAt(String name, Object value)
    {
        set(name, value);
    }
}
