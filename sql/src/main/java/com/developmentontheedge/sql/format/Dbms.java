package com.developmentontheedge.sql.format;

import com.developmentontheedge.dbms.DbmsType;

public enum Dbms
{
    DB2       ( DbmsType.DB2),
    MYSQL     ( DbmsType.MYSQL),
    ORACLE    ( DbmsType.ORACLE),
    SQLSERVER ( DbmsType.SQLSERVER),
    POSTGRESQL( DbmsType.POSTGRESQL);

    private Dbms(DbmsType type)
    {
        this.type = type;
    }

    private final DbmsType type;
    public DbmsType getType()
    {
        return type;
    }

    public String getName()
    {
        return type.getName();
    }

}
