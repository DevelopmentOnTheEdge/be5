package com.developmentontheedge.be5.metadata;

import java.sql.SQLException;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.dbms.DbmsConnector; 

/**
 * Temporary class for imported and unclassified constants from BeanExplorer EE.
 */
public class Utils 
{
	public static Logger log = Logger.getLogger(Utils.class.getName());
	
    /**
     * This method is doing same actions as {@link #toInClause(Object[] values) toInClause}
     *
     * @param values collection of the values, that needs to be in "IN" clause
     * @return surrounded values
     */
    public static String toInClause( Collection values )
    {
        return toInClause( values, false );
    }

    /**
     * This method is doing same actions as {@link #toInClause(Object[] values) toInClause}
     *
     * @param values collection of the values, that needs to be in "IN" clause
     * @param isNumeric specifies, that values in collection has numeric data type
     * @return surrounded values
     */
    public static String toInClause( Collection values, boolean isNumeric )
    {
        return toInClause( values, isNumeric, null );
    }

    /**
     * Surround specified collection values ( "(", ",", ")" ) for putting them into IN clause.
     * <br/>Example: SELECT * FROM some_table WHERE some_column IN (values[0], values[1], ...)
     *
     * <br/><br/><b>Attention!!!</b> Values (except numeric values) in the collection must be already formatted for SQL syntax.
     * If collection contains numeric data, you must set isNumeric parameter to true.
     *
     * @param isNumeric specifies, that values in collection has numeric data type
     * @param values collection of the values, that needs to be in "IN" clause
     * @param prefix string value to be added to every element in the list
     * @return surrounded values
     */
    public static String toInClause( Collection values, boolean isNumeric, String prefix )
    {
        StringBuilder clause = new StringBuilder( "(" );
        Set set = new HashSet();
        boolean first = true;
        prefix = ( prefix == null ) ? "" : prefix;
        for( Object v : values )
        {
            if ( v == null )
            {
                continue;
            }

            if( isNumeric )
            {
                try
                {
                    if( v != null && !"null".equalsIgnoreCase( v.toString() ) )
                    {
                        Double.parseDouble( v.toString() );
                    }
                }
                catch( NumberFormatException exc )
                {
                    log.log(Level.WARNING, "toInClause: Bad numeric value '" + v + "'");
                    continue;
                }
            }

            if( !set.add( v ) )
            {
                continue;
            }

            if( first )
            {
                first = false;
            }
            else
            {
                clause.append( ',' );
            }

            if( isNumeric )
            {
                clause.append( prefix ).append( v );
            }
            else
            {
                String val = v.toString();
                if( val.startsWith( "'" ) && val.endsWith( "'" ) )
                {
                    clause.append( val );
                }
                else
                {
                    clause.append( "'" ).append( prefix ).append( val ).append( "'" );
                }
            }
        }
        clause.append( ")" );
        return clause.toString();
    }
    
}
