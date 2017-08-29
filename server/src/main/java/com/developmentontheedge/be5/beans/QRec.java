package com.developmentontheedge.be5.beans;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shortcut for JDBCRecordAdapterAsQuery
 */
public class QRec extends JDBCRecordAdapterAsQuery
{
    private static final Logger log = Logger.getLogger(QRec.class.getName());

//    public QRec(DynamicPropertySet dps)
//    {
//        super( dps );
//    }
//
    public QRec()
    {
        initialized = true;
        useAddIndexes = false;
        propDisplayNameHash = null;
    }

    protected QRec(String ... initData )
    {
        initialized = true;
        useAddIndexes = false;
        propDisplayNameHash = null;
        for( int i = 0; i < initData.length; i += 2 )
        {
            try
            {
                add( new DynamicProperty( initData[ i ], initData[ i ], String.class, initData[ i + 1 ] ) );
            }
            catch( Exception exc )
            {
                log.log(Level.SEVERE, "Shouldn't happen", exc );
            }
        }
    }

//    public static QRec withCache( String sql ) throws SQLException, NoRecord
//    {
//       return withCache( sql, null );
//    }
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
