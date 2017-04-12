package com.developmentontheedge.be5.api.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Be5ErrorCode
{
    INTERNAL_ERROR, INTERNAL_ERROR_IN_OPERATION, INTERNAL_ERROR_IN_QUERY, NOT_INITIALIZED,
    UNKNOWN_COMPONENT, UNKNOWN_ENTITY, UNKNOWN_QUERY, UNKNOWN_OPERATION, NO_OPERATION_IN_QUERY,
    PARAMETER_ABSENT, PARAMETER_EMPTY, PARAMETER_INVALID, STATE_INVALID,
    ACCESS_DENIED, ACCESS_DENIED_TO_OPERATION, ACCESS_DENIED_TO_QUERY;

    private static final Logger log = Logger.getLogger(Be5ErrorCode.class.getName());

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception exception(Object... parameters)
    {
        return Be5Exception.create(this, ErrorMessages.formatMessage(this, parameters));
    }

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception exception(Logger log, Object... parameters)
    {
        String msg = ErrorMessages.formatMessage(this, parameters);
        log.severe(msg);
        return Be5Exception.create(this, msg);
    }

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception rethrow(Throwable t, Object... parameters)
    {
        String msg = ErrorMessages.formatMessage(this, parameters);
        log.log(Level.SEVERE, msg, t);
        return Be5Exception.create(this, msg, t);
    }

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception rethrow(Logger log, Throwable t, Object... parameters)
    {
        String msg = ErrorMessages.formatMessage(this, parameters);
        log.log(Level.SEVERE, msg, t);
        return Be5Exception.create(this, msg, t);
    }

    public boolean isInternal()
    {
        switch (this)
        {
        case INTERNAL_ERROR:
        case INTERNAL_ERROR_IN_OPERATION:
        case INTERNAL_ERROR_IN_QUERY:
            return true;
        default:
            return false;
        }
    }

    public boolean isAccessDenied()
    {
        switch (this)
        {
            case ACCESS_DENIED:
            case ACCESS_DENIED_TO_OPERATION:
            case ACCESS_DENIED_TO_QUERY:
                return true;
            default:
                return false;
        }
    }

    public boolean isNotFound()
    {
        switch (this)
        {
            case UNKNOWN_ENTITY:
            case UNKNOWN_COMPONENT:
            case UNKNOWN_OPERATION:
            case UNKNOWN_QUERY:
            case NO_OPERATION_IN_QUERY:
                return true;
            default:
                return false;
        }
    }

}
