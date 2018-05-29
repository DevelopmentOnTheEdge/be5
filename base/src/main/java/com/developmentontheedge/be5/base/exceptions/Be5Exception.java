package com.developmentontheedge.be5.base.exceptions;

import com.developmentontheedge.be5.util.HtmlUtils;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.Query;

/**
 * The general BeanExplorer5 exception. You can create instances of the exception with its static constructors.
 * 
 * @author lan
 */
public class Be5Exception extends RuntimeException
{
    private static final long serialVersionUID = 9189259622768482031L;

    private final String title;
    private final Be5ErrorCode code;

    private Be5Exception(Be5ErrorCode code, String title, Throwable cause)
    {
        super(title, cause);
        this.title = title;
        this.code = code;
    }

    private Be5Exception(Be5ErrorCode code, String title)
    {
        this(code, title, null);
    }

    private Be5Exception(Be5ErrorCode code, Throwable t, Object... parameters)
    {
        super(ErrorTitles.formatTitle(code, parameters), t);

        title = ErrorTitles.formatTitle(code, parameters);

        this.code = code;
    }

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
    static Be5Exception create(Be5ErrorCode code, String title, Throwable t)
    {
        return new Be5Exception(code, title, t);
    }
    
    public static Be5Exception accessDenied()
    {
        return Be5ErrorCode.ACCESS_DENIED.exception();
    }

    public static Be5Exception internal(String title)
    {
        return Be5ErrorCode.INTERNAL_ERROR.exception(title);
    }

    public static Be5Exception internal(Throwable t)
    {
        return internal(t, "");
    }

    public static Be5Exception internal(Throwable t, String title)
    {
        return Be5ErrorCode.INTERNAL_ERROR.rethrow(t, title);
    }

    public static Be5Exception internalInQuery(Throwable t, Query q)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_QUERY.rethrow(t, q.getEntity().getName(), q.getName());
    }

    public static Be5Exception internalInOperation(Throwable t, Operation o)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow( t, o.getEntity().getName(), o.getName());
    }

    public static Be5Exception internalInOperationExtender(Throwable t, OperationExtender operationExtender)
    {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION_EXTENDER.rethrow( t, operationExtender.getClassName());
    }
    
    public static Be5Exception invalidRequestParameter(String parameterName, String invalidValue)
    {
        return Be5ErrorCode.PARAMETER_INVALID.exception(parameterName, invalidValue);
    }

    public static Be5Exception invalidRequestParameter(Throwable t, String parameterName, String invalidValue)
    {
        return Be5ErrorCode.PARAMETER_INVALID.rethrow(t, parameterName, invalidValue);
    }
    
    public static Be5Exception requestParameterIsAbsent(String parameterName)
    {
        return Be5ErrorCode.PARAMETER_ABSENT.exception(parameterName);
    }

    public static Be5Exception unknownComponent(String name)
    {
        return Be5ErrorCode.UNKNOWN_COMPONENT.exception(name);
    }

    public static Be5Exception unknownEntity(String entityName)
    {
        return Be5ErrorCode.UNKNOWN_ENTITY.exception(entityName);
    }

    public static Be5Exception unknownQuery(String entityName, String queryName)
    {
        return Be5ErrorCode.UNKNOWN_QUERY.exception(entityName, queryName);
    }
    
    public static Be5Exception invalidState(String title)
    {
        return Be5ErrorCode.STATE_INVALID.exception(title);
    }

    public Be5ErrorCode getCode()
    {
        return code;
    }

    public String getTitle()
    {
        return title;
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

    public static String getFullStackTraceLine(StackTraceElement e)
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

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return code == that.code;
    }

    @Override
    public int hashCode()
    {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}
