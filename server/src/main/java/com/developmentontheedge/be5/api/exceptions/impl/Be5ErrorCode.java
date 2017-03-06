package com.developmentontheedge.be5.api.exceptions.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;

public enum Be5ErrorCode
{
    INTERNAL_ERROR, INTERNAL_ERROR_IN_OPERATION, INTERNAL_ERROR_IN_QUERY, NOT_INITIALIZED, UNKNOWN_COMPONENT, UNKNOWN_ENTITY, UNKNOWN_QUERY, UNKNOWN_OPERATION, PARAMETER_ABSENT, PARAMETER_EMPTY, PARAMETER_INVALID, STATE_INVALID, ACCESS_DENIED, ACCESS_DENIED_TO_OPERATION, ACCESS_DENIED_TO_QUERY, NO_OPERATION_IN_QUERY;

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
    public Be5Exception rethrow(Throwable t, Object... parameters)
    {
        return Be5Exception.create(this, ErrorMessages.formatMessage(this, parameters), t);
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

}
