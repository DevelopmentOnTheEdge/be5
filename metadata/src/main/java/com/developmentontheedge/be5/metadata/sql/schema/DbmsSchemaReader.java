package com.developmentontheedge.be5.metadata.sql.schema;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

public interface DbmsSchemaReader
{
    String getDefaultSchema(BeSqlExecutor sql) throws ExtendedSqlException;

    Map<String, String> readTableNames( BeSqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException, ProcessInterruptedException;

    Map<String, List<SqlColumnInfo>> readColumns( BeSqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException, ProcessInterruptedException;

    Map<String, List<IndexInfo>> readIndices( BeSqlExecutor sql, String defSchema, ProcessController controller ) throws SQLException, ProcessInterruptedException;
}
