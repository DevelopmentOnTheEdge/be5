package com.developmentontheedge.be5.base.exceptions;


public enum Be5ErrorCode
{
    INTERNAL_ERROR, INTERNAL_ERROR_IN_OPERATION, INTERNAL_ERROR_IN_OPERATION_EXTENDER,
    INTERNAL_ERROR_IN_QUERY, NOT_INITIALIZED,
    UNKNOWN_COMPONENT, UNKNOWN_ENTITY, UNKNOWN_QUERY, UNKNOWN_OPERATION, NO_OPERATION_IN_QUERY, NOT_FOUND,
    PARAMETER_ABSENT, PARAMETER_EMPTY, PARAMETER_INVALID, STATE_INVALID,
    ACCESS_DENIED, ACCESS_DENIED_TO_OPERATION, ACCESS_DENIED_TO_QUERY;

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    public Be5Exception exception(Object... parameters)
    {
        String msg = ErrorTitles.formatTitle(this, parameters);

        return Be5Exception.create(this, msg);
    }

    /**
     * Creates a {@link Be5Exception} by the code and a formatted message. Note
     * that this method is not a part of the API.
     */
    Be5Exception rethrow(Throwable t, Object... parameters)
    {
        String msg = ErrorTitles.formatTitle(this, parameters);

        return Be5Exception.create(this, msg, t);
    }

    public boolean isInternal()
    {
        switch (this)
        {
        case INTERNAL_ERROR:
        case INTERNAL_ERROR_IN_OPERATION:
        case INTERNAL_ERROR_IN_QUERY:
        case NOT_INITIALIZED:
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
            case NOT_FOUND:
                return true;
            default:
                return false;
        }
    }

    private final static String HTTP_CODE_404 = "404";
    private final static String HTTP_CODE_403 = "403";
    private final static String HTTP_CODE_500 = "500";

    public String getHttpStatus()
    {
        if (isNotFound())return HTTP_CODE_404;
        if (isAccessDenied())return HTTP_CODE_403;
        return HTTP_CODE_500;
    }
}
