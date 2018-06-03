package com.developmentontheedge.be5.metadata.operations;


public class DatabaseTargetException extends RuntimeException
{
    public DatabaseTargetException(String msg)
    {
        super(msg);
    }

    public DatabaseTargetException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
