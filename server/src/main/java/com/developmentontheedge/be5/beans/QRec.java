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


}
