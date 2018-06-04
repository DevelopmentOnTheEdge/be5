package com.developmentontheedge.be5.metadata.scripts;


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
