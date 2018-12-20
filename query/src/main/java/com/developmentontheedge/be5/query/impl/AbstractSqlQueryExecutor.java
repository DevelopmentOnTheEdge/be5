package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.SqlQueryExecutor;
import com.developmentontheedge.sql.model.AstStart;
import org.apache.commons.dbutils.ResultSetHandler;

import java.util.Objects;


public abstract class AbstractSqlQueryExecutor extends AbstractOrderedQueryExecutor implements SqlQueryExecutor
{
    protected final Query query;

    protected Boolean selectable = false;

    public AbstractSqlQueryExecutor(Query query)
    {
        this.query = Objects.requireNonNull(query);
    }

    @Override
    public Boolean isSelectable()
    {
        return selectable;
    }

    public abstract <T> T query(ResultSetHandler<T> rsh);

    public abstract AstStart getFinalSql();
}
