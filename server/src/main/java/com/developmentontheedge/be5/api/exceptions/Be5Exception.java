package com.developmentontheedge.be5.api.exceptions;

import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.impl.ErrorMessages;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * The general BeanExplorer5 exception. You can create instances of the exception with its static constructors.
 * 
 * @author lan
 */
public class Be5Exception extends RuntimeException
{
    
    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    public static Be5Exception create(Be5ErrorCode code, String message)
    {
        return new Be5Exception(code, message);
    }
    
    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    public static Be5Exception create(Be5ErrorCode code, String message, Throwable t)
    {
        return new Be5Exception(code, message, t);
    }
    
    public static Be5Exception accessDenied()
    {
        return Be5ErrorCode.ACCESS_DENIED.exception();
    }
    
    public static Be5Exception internal(String message)
    {
        return new Be5Exception(Be5ErrorCode.INTERNAL_ERROR, message);
    }

    public static Be5Exception internal(Throwable t)
    {
        return internal(t, t.getMessage());
    }

    public static Be5Exception internal(Throwable t, String message)
    {
        return new Be5Exception(Be5ErrorCode.INTERNAL_ERROR, t, message);
    }

    public static Be5Exception internalInQuery(Throwable t, Query q)
    {
        return new Be5Exception(Be5ErrorCode.INTERNAL_ERROR_IN_QUERY, t, q.getEntity().getName(), q.getName(), t.getMessage());
    }

    public static Be5Exception internalInOperation(Throwable t, Operation o)
    {
        return new Be5Exception(Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION, t, o.getEntity().getName(), o.getName(), t.getMessage());
    }
    
    public static Be5Exception invalidRequestParameter(String parameterName, String invalidValue)
    {
        return Be5ErrorCode.PARAMETER_INVALID.exception(parameterName, invalidValue);
    }
    
    public static Be5Exception requestParameterIsAbsent(String parameterName)
    {
        return Be5ErrorCode.PARAMETER_ABSENT.exception(parameterName);
    }
    
    public static Be5Exception invalidState(String message)
    {
        return Be5ErrorCode.STATE_INVALID.exception(message);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 9189259622768482031L;

    private final Be5ErrorCode code;

    private Be5Exception(Be5ErrorCode code, String message, Throwable cause)
    {
        super(message, cause);
        this.code = code;
    }

    private Be5Exception(Be5ErrorCode code, String message)
    {
        this(code, message, null);
    }

    private Be5Exception(Be5ErrorCode code, Throwable t, Object... parameters)
    {
        super(ErrorMessages.formatMessage(code, parameters), t);
        this.code = code;
    }
    
    public Be5ErrorCode getCode()
    {
        return code;
    }
    
}
