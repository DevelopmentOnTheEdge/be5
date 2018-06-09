package com.developmentontheedge.be5.base.exceptions;

import com.developmentontheedge.be5.base.util.HtmlUtils;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.Query;


public class Be5Exception extends RuntimeException
{
    private static final long serialVersionUID = 9189259622768482031L;

    private final String message;
    private final Be5ErrorCode code;

    private Be5Exception(Be5ErrorCode code, String message, Throwable cause)
    {
        super(message, cause);
        this.message = message;
        this.code = code;
    }

    private Be5Exception(Be5ErrorCode code, String message)
    {
        this(code, message, null);
    }

//    private Be5Exception(Be5ErrorCode code, Throwable t, Object... parameters)
//    {
//        super(ErrorTitles.formatTitle(code, parameters), t);
//
//        title = ErrorTitles.formatTitle(code, parameters);
//
//        this.code = code;
//    }

    /**
     * Not a part of the API as you can't create {@link Be5ErrorCode}.
     */
    static Be5Exception create(Be5ErrorCode code, String title)
    {
        return new Be5Exception(code, title);
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

    public static Be5Exception accessDeniedToOperation(String entityName, String operationName)
    {
        return Be5ErrorCode.ACCESS_DENIED_TO_OPERATION.exception(entityName, operationName);
    }

    public static Be5Exception accessDeniedToQuery(String entityName, String queryName)
    {
        return Be5ErrorCode.ACCESS_DENIED_TO_QUERY.exception(entityName, queryName);
    }

    public static Be5Exception internal(String title)
    {
        return Be5ErrorCode.INTERNAL_ERROR.exception(title);
    }

    public static Be5Exception internal(Throwable cause)
    {
        return internal("", cause);
    }

    public static Be5Exception internal(String message, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(cause, message);
    }

    public static Be5Exception internalInQuery(Query q, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_QUERY.rethrow(cause, q.getEntity().getName(), q.getName());
    }

    public static Be5Exception internalInOperation(Operation o, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(cause, o.getEntity().getName(), o.getName());
    }

    public static Be5Exception operationNotAssignedToQuery(String entityName, String queryName, String name)
    {
        return Be5ErrorCode.OPERATION_NOT_ASSIGNED_TO_QUERY.exception(entityName, queryName, name);
    }

    public static Be5Exception internalInOperationExtender(OperationExtender operationExtender, Throwable cause)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION_EXTENDER.rethrow(cause, operationExtender.getClassName());
    }

    public static Be5Exception unknownEntity(String entityName)
    {
        return Be5ErrorCode.UNKNOWN_ENTITY.exception(entityName);
    }

    public static Be5Exception unknownQuery(String entityName, String queryName)
    {
        return Be5ErrorCode.UNKNOWN_QUERY.exception(entityName, queryName);
    }

    public static Be5Exception unknownOperation(String entityName, String operationName)
    {
        return Be5ErrorCode.UNKNOWN_OPERATION.exception(entityName, operationName);
    }

    public static Be5Exception notFound(String element)
    {
        return Be5ErrorCode.NOT_FOUND.exception(element);
    }

    public static Be5Exception invalidState(String title)
    {
        return Be5ErrorCode.INVALID_STATE.exception(title);
    }

    public Be5ErrorCode getCode()
    {
        return code;
    }

    public String getTitle()
    {
        return message;
    }

    public static String getMessage(Throwable err)
    {
        Throwable e = err;
        StringBuilder out = new StringBuilder(getThrowableMessage(e));

        while(e instanceof Be5Exception && e.getCause() != null)
        {
            e = e.getCause();
            out.append(getThrowableMessage(e));
        }
        if(e.getClass() == NullPointerException.class)
        {
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 2); i++)
            {
                out.append(getFullStackTraceLine(stackTrace[i])).append("\n");
            }
        }

        return HtmlUtils.escapeHTML(out.toString());
    }

    private static String getFullStackTraceLine(StackTraceElement e)
    {
        return e.getClassName() + "." + e.getMethodName()
                + "(" + e.getFileName() + ":" + e.getLineNumber() + ")";
    }

    private static String getThrowableMessage(Throwable e)
    {
        if(e instanceof Be5Exception)
        {
            return e.getClass().getSimpleName() + ": " + e.getMessage() + "\n";
        }
        else
        {
            return e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n";
        }
    }

    public String getHttpStatusCode()
    {
        return code.getHttpStatus();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Be5Exception that = (Be5Exception) o;

        return (message != null ? message.equals(that.message) : that.message == null) && code == that.code;
    }

    @Override
    public int hashCode()
    {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}
