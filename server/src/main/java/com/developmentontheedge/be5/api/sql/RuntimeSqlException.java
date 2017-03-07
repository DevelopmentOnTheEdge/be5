package com.developmentontheedge.be5.api.sql;

import java.sql.SQLException;

public class RuntimeSqlException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public RuntimeSqlException(SQLException e)
    {
        super(e);
    }

}
