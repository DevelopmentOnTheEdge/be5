package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.sql.DpsRecordAdapter;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.model.QRec;


public class QRecServiceImpl implements QRecService
{
    private final SqlService db;

    public QRecServiceImpl(SqlService db)
    {
        this.db = db;
    }

    @Override
    public QRec of(String sql, Object... params)
    {
        return db.select(sql, (rs) -> DpsRecordAdapter.addDp(new QRec(), rs), params);
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
