package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.DpsRecordAdapter;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.DpsExecutor;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.beans.DynamicPropertySet;
import one.util.streamex.StreamEx;

import java.sql.*;
import java.util.*;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

import static com.developmentontheedge.be5.api.helpers.DpsRecordAdapter.createDps;

public class DpsExecutorImpl implements DpsExecutor
{

    @FunctionalInterface
    public interface MetaProcessor
    {
        void process(Object value, Map<String, Map<String, String>> meta);
    }

    private final DatabaseService databaseService;
    private final SqlService db;

    public DpsExecutorImpl(DatabaseService databaseService, SqlService db)
    {
        this.databaseService = databaseService;
        this.db = db;
    }

    @Override
    public List<DynamicPropertySet> list(String sql, MetaProcessor metaProcessor) {
        return db.selectList(sql, rs -> createDps(rs, metaProcessor));
    }

    @Override
    public DynamicPropertySet get(String sql)
    {
        return get(sql, (a,b)->{});
    }

    @Override
    public DynamicPropertySet get(String sql, MetaProcessor metaProcessor)
    {
        return db.select(sql, rs -> createDps(rs, metaProcessor));
    }

    @Override
    public StreamEx<DynamicPropertySet> stream(String sql, MetaProcessor metaProcessor)
    {
        ResultSet rs = null;
        try
        {
            rs = databaseService.executeQuery(sql);
            ResultSet finalRs = rs;
            return StreamEx.of( new AbstractSpliterator<DynamicPropertySet>(Long.MAX_VALUE,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE)
            {
                @Override
                public boolean tryAdvance(Consumer<? super DynamicPropertySet> action)
                {
                    try
                    {
                        if( !finalRs.next() )
                        {
                            databaseService.close( finalRs );
                            return false;
                        }
                        action.accept(DpsRecordAdapter.createDps( finalRs, metaProcessor ));
                        return true;
                    }
                    catch( Throwable t )
                    {
                        databaseService.close( finalRs );
                        throw new RuntimeException(t);
                    }
                }
            } ).onClose( () -> databaseService.close( finalRs ) );
        }
        catch( Exception e )
        {
            databaseService.close( rs );
            throw new RuntimeException(e);
        }
    }

}
