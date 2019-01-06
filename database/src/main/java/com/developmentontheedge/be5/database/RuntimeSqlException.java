package com.developmentontheedge.be5.database;

public class RuntimeSqlException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public RuntimeSqlException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RuntimeSqlException(Throwable cause)
    {
        super(cause);
    }
}
