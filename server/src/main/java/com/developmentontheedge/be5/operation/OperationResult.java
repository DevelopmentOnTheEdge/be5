package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.FrontendAction;

public class OperationResult
{
    ///////////////////////////////////////////////////////////////////
    // immutable properties
    //
    
    private OperationStatus status;
    private FrontendAction frontendAction = FrontendAction.defaultAction();

    public OperationStatus getStatus()  { return status; }
    
    private String message;
    public String getMessage()          { return message; }

    private Object details;
    public Object getDetails()          { return details; }

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
        this(status, getLocalisedMessage(status), details);
    }

    private OperationResult(OperationStatus status)
    {
        this(status, getLocalisedMessage(status), null);
    }
  
    private static String getLocalisedMessage(OperationStatus status)
    {
        return null;
    }

    private String getLocalisedMessage(String message, Object ... params)
    {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    // OperationResult factory methods
    //
    
    public static OperationResult cancelled()
    {
        return new OperationResult(OperationStatus.CANCELLED);
    }

    public static OperationResult progress()
    {
        return new OperationResult(OperationStatus.IN_PROGRESS);
    }
    
    public static OperationResult progress(String message)
    {
        return new OperationResult(OperationStatus.IN_PROGRESS, message); 
    }

    public static OperationResult progress(double preparedness)
    {
        return new OperationResult(OperationStatus.IN_PROGRESS, new Double(preparedness));
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
        return new OperationResult(OperationStatus.FINISHED); 
    }
    
    public static OperationResult finished(String message)
    {
        return new OperationResult(OperationStatus.FINISHED, message); 
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
        return new OperationResult(OperationStatus.ERROR, details); 
    }

    public FrontendAction getFrontendAction()
    {
        return frontendAction;
    }
}
