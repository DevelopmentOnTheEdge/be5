package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.util.HashUrl;

public class OperationResult
{
    ///////////////////////////////////////////////////////////////////
    // immutable properties
    //
    
    private final OperationStatus status;
    private final String message;
    private final Object details;

    ///////////////////////////////////////////////////////////////////
    // private constructors
    //

    private OperationResult(OperationStatus status, String message, Object details)
    {
        this.status  = status;
        this.message = message;
        this.details = details;
    }
    
    private OperationResult(OperationStatus status, Object details)
    {
        this(status, status.name(), details);
    }

    private OperationResult(OperationStatus status)
    {
        this(status, null, null);
    }

    public OperationStatus getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }

    public Object getDetails()
    {
        return details;
    }

//    private static String getLocalisedMessage(OperationStatus status)
//    {
//        return null;
//    }
//
//    private String getLocalisedMessage(String message, Object ... params)
//    {
//        return null;
//    }

    ///////////////////////////////////////////////////////////////////
    // OperationResult factory methods
    //

    private static final OperationResult open = new OperationResult(OperationStatus.OPEN);
    private static final OperationResult finished = new OperationResult(OperationStatus.FINISHED);
    private static final OperationResult cancelled = new OperationResult(OperationStatus.CANCELLED);
    private static final OperationResult progress = new OperationResult(OperationStatus.IN_PROGRESS);

    public static OperationResult open()
    {
        return open;
    }

    public static OperationResult cancelled()
    {
        return cancelled;
    }

    public static OperationResult progress()
    {
        return progress;
    }
    
    public static OperationResult progress(String message)
    {
        return new OperationResult(OperationStatus.IN_PROGRESS, message, "");
    }

    public static OperationResult progress(double preparedness)
    {
        return new OperationResult(OperationStatus.IN_PROGRESS, preparedness);
    }

    public static OperationResult interrupting()
    {
        return new OperationResult(OperationStatus.INTERRUPTING); 
    }

    public static OperationResult interrupted()
    {
        return new OperationResult(OperationStatus.INTERRUPTED); 
    }

    public static OperationResult finished()
    {
        return finished;
    }
    
    public static OperationResult finished(String message)
    {
        return new OperationResult(OperationStatus.FINISHED, message, null);
    }

    public static OperationResult redirect(HashUrl hashUrl)
    {
        return new OperationResult(OperationStatus.REDIRECTED, hashUrl.toString());
    }

    public static OperationResult redirect(String url)
    {
        return new OperationResult(OperationStatus.REDIRECTED, url);
    }
    
    public static OperationResult error(String message, Throwable details)
    {
        return new OperationResult(OperationStatus.ERROR, message, details); 
    }

    public static OperationResult error(Throwable details)
    {
        return new OperationResult(OperationStatus.ERROR, details.getMessage(), details.toString());
    }

    public static OperationResult error(String message)
    {
        return new OperationResult(OperationStatus.ERROR, message, null);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationResult that = (OperationResult) o;

        if (status != that.status) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return details != null ? details.equals(that.details) : that.details == null;
    }

    @Override
    public int hashCode()
    {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "OperationResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", details=" + details +
                '}';
    }
}
