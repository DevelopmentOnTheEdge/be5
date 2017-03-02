package com.developmentontheedge.sql.format;

public class Context
{
    public Context(Dbms dbms)
    {
        this.dbms = dbms;
        
        switch(dbms)
        {
        case DB2:        dbmsTransformer = new DB2Transformer();          break; 
        case MYSQL:      dbmsTransformer = new MySqlTransformer();        break; 
        case ORACLE:     dbmsTransformer = new OracleTransformer();       break; 
        case POSTGRESQL: dbmsTransformer = new PostgreSqlTransformer();   break; 
        case SQLSERVER:  dbmsTransformer = new SqlServerTransformer();    break; 
        }
    }

    protected Dbms dbms;
    public Dbms getDbms()
    {
        return dbms;
    }
    
    protected DbmsTransformer dbmsTransformer;
    public DbmsTransformer getDbmsTransformer()
    {
        return dbmsTransformer;        
    }
}
