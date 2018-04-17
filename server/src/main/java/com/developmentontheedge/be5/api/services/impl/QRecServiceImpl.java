package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.be5.query.impl.model.Be5QueryExecutor;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Collections;
import java.util.List;


public class QRecServiceImpl implements QRecService
{
    private final SqlService db;
    private final Meta meta;
    private final Injector injector;

    public QRecServiceImpl(SqlService db, Meta meta, Injector injector)
    {
        this.db = db;
        this.meta = meta;
        this.injector = injector;
    }

    @Override
    public QRec of(String sql, Object... params)
    {
        DynamicPropertySet dps = db.select(sql, DpsRecordAdapter::createDps, params);

        if(dps == null)
        {
            return null;
        }
        else
        {
            QRec qRec = new QRec();
            for (DynamicProperty property : dps)
            {
                qRec.add(property);
            }
            return qRec;
        }
    }

    public QRec withCache( String sql, Object... params )
    {
        throw Be5Exception.internal("not implemented");
        //return withCache( sql, null );
    }
//
//    public static QRec withCache( String sql, String key ) throws SQLException, NoRecord
//    {
//        QRec ret = ( QRec )cache.get( key == null ? sql : key );
//        if( ret != null )
//        {
//            if( ret.isEmpty() )
//            {
//                throw new NoRecord( "No record produced by QRec, SQL query is " + sql );
//            }
//            return ret;
//        }
//
//        try
//        {
//            ret = new QRec();
//            for( DynamicProperty prop : new QRec( connector, sql ) )
//            {
//                ret.add( new DynamicProperty( prop.getName(), prop.getType(), prop.getValue() ) );
//            }
//            cache.put( key == null ? sql : key, ret );
//        }
//        catch( QRec.NoRecord nr )
//        {
//            cache.put( key == null ? sql : key, new QRec() );
//            throw new NoRecord( "No record produced by QRec, SQL query is " + sql );
//        }
//        return ret;
//    }
}
