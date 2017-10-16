package com.developmentontheedge.be5.api.exceptions;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.OperationInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

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
    static Be5Exception create(Be5ErrorCode code, String message)
    {
        return new Be5Exception(code, message);
    }
    
    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    static Be5Exception create(Be5ErrorCode code, String message, Throwable t)
    {
        return new Be5Exception(code, message, t);
    }
    
    public static Be5Exception accessDenied()
    {
        return Be5ErrorCode.ACCESS_DENIED.exception();
    }
    
    public static Be5Exception internal(String message)
    {
        return Be5ErrorCode.INTERNAL_ERROR.exception(message);
    }

    public static Be5Exception internal(Logger log, String message)
    {
        return Be5ErrorCode.INTERNAL_ERROR.exception(log, message);
    }

    public static Be5Exception internal(Logger log, Throwable t)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(log, t);
    }

    public static Be5Exception internal(Logger log, Throwable t, Object... parameters)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(log, t, parameters);
    }

    public static Be5Exception internal(Throwable t)
    {
        return internal(t, t.getMessage());
    }

    public static Be5Exception internal(Throwable t, String message)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(t, message);
    }

    public static Be5Exception internalInQuery(Throwable t, Query q)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_QUERY.rethrow(t, q.getEntity().getName(), q.getName());
    }

    public static Be5Exception internalInOperation(Throwable t, OperationInfo o)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow( t, o.getEntity().getName(), o.getName());
    }
    
    public static Be5Exception invalidRequestParameter(String parameterName, String invalidValue)
    {
        return Be5ErrorCode.PARAMETER_INVALID.exception(parameterName, invalidValue);
    }

    public static Be5Exception invalidRequestParameter(Logger log, Throwable t, String parameterName, String invalidValue)
    {
        return Be5ErrorCode.PARAMETER_INVALID.rethrow(log, t, parameterName, invalidValue);
    }
    
    public static Be5Exception requestParameterIsAbsent(String parameterName)
    {
        return Be5ErrorCode.PARAMETER_ABSENT.exception(parameterName);
    }

    public static Be5Exception unknownComponent(String name)
    {
        return Be5ErrorCode.UNKNOWN_COMPONENT.exception(name);
    }

    public static Be5Exception unknownEntity(String name)
    {
        return Be5ErrorCode.UNKNOWN_ENTITY.exception(name);
    }
    
    public static Be5Exception invalidState(String message)
    {
        return Be5ErrorCode.STATE_INVALID.exception(message);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 9189259622768482031L;

    private final String title;
    private final Be5ErrorCode code;

    private Be5Exception(Be5ErrorCode code, String message, Throwable cause)
    {
        super(message, cause);
        title = message;
        this.code = code;
    }

    private Be5Exception(Be5ErrorCode code, String message)
    {
        this(code, message, null);
    }

    private Be5Exception(Be5ErrorCode code, Throwable t, Object... parameters)
    {
        super(ErrorMessages.formatMessage(code, parameters), t);

        title = ErrorMessages.formatMessage(code, parameters);

        this.code = code;
    }
    
    public Be5ErrorCode getCode()
    {
        return code;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Be5Exception that = (Be5Exception) o;

        return code == that.code;
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    public String getTitle()
    {
        return title;
    }

    public static String exceptionAsString(Throwable e)
    {
        StringWriter sw = new StringWriter();
        if(e instanceof Be5Exception)
        {
            e.getCause().printStackTrace(new PrintWriter(sw));
        }
        else
        {
            e.printStackTrace(new PrintWriter(sw));
        }
        return sw.toString();
    }
}
