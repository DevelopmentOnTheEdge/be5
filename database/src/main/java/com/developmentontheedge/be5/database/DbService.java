package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.TransactionExecutor;
import com.developmentontheedge.be5.database.sql.TransactionExecutorVoid;
import com.developmentontheedge.be5.database.sql.parsers.ScalarLongParser;
import com.developmentontheedge.be5.database.sql.parsers.ScalarParser;
import com.developmentontheedge.be5.database.util.SqlUtils;
import com.developmentontheedge.sql.model.AstStart;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.annotation.Nullable;
import java.util.List;


public interface DbService
{
    /**
     * Execute an sql select query with replacement parameters.
     *
     * @param sql query to execute.
     * @param rsh handler that converts the results into an object.
     * @param params replacement parameters.
     * @param <T> type of object that the handler returns
     * @return object returned by the handler.
     */
    @Nullable
    <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);

    @Nullable
    <T> T select(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> list(String sql, ResultSetParser<T> parser, Object... params);

    <T> List<T> list(AstStart astStart, ResultSetParser<T> parser, Object... params);

    @Nullable
    <T> T one(String sql, Object... params);

    /**
     * Execute an sql insert, update, or delete query.
     *
     * @param sql a sql to execute.
     * @param params query replacement parameters.
     * @return number of rows updated.
     */
    int update(String sql, Object... params);

    int updateUnsafe(String sql, Object... params);

    /**
     * Executes the given insert sql statement.
     *
     * @param sql a sql statement to execute.
     * @param params query replacement parameters.
     * @param <T> type of object that the handler returns
     * @return object generated by the handler.
     */
    <T> T insert(String sql, Object... params);

    String format(String sql);

    String format(AstStart astStart);

    <T> T execute(SqlExecutor<T> executor);

    /**
     * Executes <code>executor</code> in a transaction, and returns the result of the callback.
     *
     * @param executor a callback which will receive an open connection, in a transaction.
     * @param <T> type returned by callback
     * @return value returned from the executor
     */
    <T> T inTransaction(TransactionExecutor<T> executor);

    /**
     * Executes <code>executor</code> in a transaction.
     *
     * @param executor a callback which will receive an open connection, in a transaction.
     */
    void useTransaction(TransactionExecutorVoid executor);

    @Nullable
    default Long oneLong(String sql, Object... params)
    {
        return SqlUtils.longFromDbObject(one(sql, params));
    }

    default long countFrom(String sql, Object... params)
    {
        if (!(sql.startsWith("SELECT COUNT(1) FROM ") || sql.startsWith("SELECT count(1) FROM ") ||
                sql.startsWith("SELECT COUNT(*) FROM ") || sql.startsWith("SELECT count(*) FROM ") ||
                sql.startsWith("SELECT COUNT(*) AS \"count\" FROM ")))
        {
            sql = "SELECT COUNT(1) FROM " + sql;
        }
        return SqlUtils.longFromDbObject(one(sql, params));
    }

    @Nullable
    default String oneString(String sql, Object... params)
    {
        return SqlUtils.stringFromDbObject(one(sql, params));
    }

    @Nullable
    default Integer oneInteger(String sql, Object... params)
    {
        return one(sql, params);
    }

    default <T> List<T> scalarList(String sql, Object... params)
    {
        return list(sql, new ScalarParser<T>(), params);
    }

    default List<Long> scalarLongList(String sql, Object... params)
    {
        return list(sql, new ScalarLongParser(), params);
    }

    default Long[] longArray(String sql, Object... params)
    {
        return scalarLongList(sql, params).toArray(new Long[0]);
    }

    default String[] stringArray(String sql, Object... params)
    {
        return list(sql, new ScalarParser<String>(), params).toArray(new String[0]);
    }

}
