/** $Id: DatabaseConnector.java,v 1.70 2014/04/25 10:57:55 zha Exp $ */

package com.developmentontheedge.be5.metadata.sql;

import com.developmentontheedge.be5.metadata.model.Operation;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface DatabaseConnector extends Serializable
{
    ResultSet executeQuery(String query) throws SQLException;
    ResultSet updatableQuery(String query) throws Exception;
    ResultSet executeQuery(String query, boolean bStreaming) throws SQLException;
    //ResultSet executeQuery(String query, boolean bStreaming, Operation.InterruptMonitor monitor) throws Exception;
    //ResultSet executeQuery(String query, boolean bStreaming, ValueHolder<Statement> statementHolder, boolean bUpdatable) throws Exception;
    void close(ResultSet rs);

    int executeUpdate(String sql) throws SQLException;
    int[] executeBatch(List<String> sqls) throws SQLException;

    String executeInsert(String sql) throws SQLException;
    String executeInsert(String sql, String pk) throws SQLException;

    boolean isInTransaction() throws SQLException;
    void startTransaction() throws SQLException;
    void commitTransaction() throws SQLException;
    void rollbackTransaction() throws SQLException;

    String getEncoding();
    void setEncoding(String encoding);

    void setSlowQueryThreshold(int value);
    void setForceDateFormat(boolean forceDateFormat);

    String getConnectString();
    void setConnectString(String url);

    String getConnectionUserName() throws SQLException;
    Connection getConnection() throws SQLException;
    void closeConnection(Connection connection) throws SQLException;
    void releaseConnection(Connection connection) throws SQLException;

    boolean isMySQL();
    boolean isMySQL5();
    boolean isMySQL41();

    boolean isODBC();
   
    boolean isSQLite();

    boolean isOracle();
    boolean isOracle8();

    boolean isSQLServer();
    boolean isSQLServer2005();
    boolean isSQLServerJTDS();

    boolean isDb2();
    boolean isDb2NetDriver();
    boolean isDb2AppDriver();    
    boolean isDb2v8();
    boolean isDb2v9();

    boolean isPostgreSQL();

    String getDatabaseName();
    
    //TODO DatabaseAnalyzer getAnalyzer();

    String NL();

    /**
     * Connector to replicated database should not be used for update or insert.
     *
     * @return true is database modification is prohibited for this connector.
     */
    boolean isOnlyForSelect();
    void setOnlyForSelect(boolean b);
    
    /**
     * Execute statements encapsulated by sqlStatementsExecuter in single transaction. 
     * 
     * @param sqlStatementsExecutor
     * @throws Exception
     */

//TODO    void transaction(SQLStatementsExecutor sqlStatementsExecutor) throws Exception;
//
//    <T> T transaction(SQLStatementCaller<T> sqlStatementsCaller) throws Exception;
//
//    public static enum CommitType
//    {
//        SYNC_COMMIT,
//        ASYNC_COMMIT
//    }
//
//    void transaction(SQLStatementsExecutor sqlStatementsExecutor, CommitType commitType) throws Exception;
//    <T> T transaction(SQLStatementCaller<T> sqlStatementsCaller, CommitType commitType) throws Exception;
}
