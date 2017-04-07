package com.developmentontheedge.be5.metadata;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.DbmsType;
//import com.developmentontheedge.be5.metadata.caches.Cache;
//import com.developmentontheedge.be5.metadata.caches.SystemSettingsCache;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Temporary class for imported and unclassified constants from BeanExplorer EE.
 */
public class Utils 
{
	public static final Logger log = Logger.getLogger(Utils.class.getName());

    static String MISSING_SETTING_VALUE = "some-absolutely-impossible-setting-value";

    /**
     * Shows not very long stacktrace of exception without misleading "Exception" word in it.
     * @param exc
     * @return
     */
    public static String trimStackAsString( Throwable exc )
    {
        return trimStackAsString( exc, 7 );
    }

    public static String trimStackAsString( Throwable exc, int nLines )
    {
        StringBuilder sb = new StringBuilder();

        List<StackTraceElement> stackList = Arrays.asList( exc.getStackTrace() );
        if( stackList.size() > nLines )
        {
            stackList = stackList.subList( 0, nLines );
        }

        for( StackTraceElement stackEl : stackList )
        {
            sb.append( "     at " ).append( stackEl.toString() ).append( "\n" );
        }
        return sb.toString();
    }
	
    /**
     *
     * @param values collection of the values, that needs to be in "IN" clause
     * @return surrounded values
     */
    public static String toInClause( Collection values )
    {
        return toInClause( values, false );
    }

    /**
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

    /**
     * Prepare string text for MySQL to understand it as string.
     *
     * @param text text, that is preparing to be sql text
     * @return returns generated sql string
     */
    public static String safestrMySQL( String text )
    {
        StringBuffer to = new StringBuffer();
        char from[] = text.toCharArray();

        for( int i = 0; i < from.length; i++ )
        {
            if( from[ i ] == '\'' )
            {
                to.append( "''" );
            }
            else if( from[ i ] == '\\' )
            {
                to.append( "\\\\" );
            }
            else
            {
                to.append( from[ i ] );
            }
        }

        return to.toString();
    }

    /**
     * Prepare string text for Db2 to understand it as string.
     *
     * @param text text, that is preparing to be sql text
     * @return returns generated sql string
     */
    public static String safestrDb2( String text )
    {
        StringBuffer to = new StringBuffer();
        char from[] = text.toCharArray();

        for( int i = 0; i < from.length; i++ )
        {
            if( from[ i ] == '\'' )
            {
                to.append( "''" );
            }
            else
            {
                to.append( from[ i ] );
            }
        }

        return to.toString();
    }

    /**
     * Prepare string text for sql to understand it as string.
     * Sql type is taken from "connector".
     * This function can also quote generated string, if "quote" is enabled.
     *
     * @param connector connector to the database
     * @param text text, that is preparing to be sql text
     * @param quote quoting formed sql string
     * @return returns generated sql string
     */
    public static String safestr( DbmsConnector connector, String text, boolean quote )
    {
        if( text == null )
        {
            return null;
        }

        String newText;

        if( connector != null && ( connector.getType() == DbmsType.MYSQL ) )
        {
            newText = safestrMySQL( text );
        }
        else
        {
            newText = safestrDb2( text );
        }

        if( quote )
        {
            return "'" + newText + "'";
        }
        else
        {
            return newText;
        }
    }

    /**
     *
     * @param connector
     * @param param parameter name
     * @return
     */
    public static String getSystemSetting( DbmsConnector connector, String param )
    {
        return getSystemSettingInSection( connector, "system", param, null );
    }

    /**
     *
     * @param connector
     * @param param parameter name
     * @param defValue this value is returned, when such parameter does not exists in DB
     * @return
     */
    public static String getSystemSetting( DbmsConnector connector, String param, String defValue )
    {
        return getSystemSettingInSection( connector, "system", param, defValue );
    }

    /**
     * This method is working like calling method {@link #getSystemSettingInSection(DbmsConnector, String, String) getSystemSettingInSection}
     * with parameter defValue = null
     *
     * @param connector DB connector
     * @param section system settings section name
     * @param param parameter name
     * @return section parameter value
     */
    public static String getSystemSettingInSection( DbmsConnector connector, String section, String param )
    {
        return getSystemSettingInSection( connector, section, param, null );
    }

    /**
     * Retrieving system settings parameter value for specified section and parameter. If there isn't such parameter, or
     * executing query throws any exception, then method will return defValue.
     * <br/>Results of method call are cached.
     *
     * <b>Attention!!! In this method:</b>
     * <br/> - parameter section is processing by {@link #safestr(DbmsConnector, String, boolean) safestr}
     * <br/> - parameter param is not processing by {@link #safestr(DbmsConnector, String, boolean) safestr}
     *
     * @param connector DB connector
     * @param section system settings section name
     * @param param parameter name
     * @param defValue default value for return, if there isn't such section or parameter
     * @return section parameter value
     */
    // it is deliberately not synchronized!
    // it is better to let 2 processes to do the same thing twice than
    // to block on network call
    public static String getSystemSettingInSection(DbmsConnector connector, String section, String param, String defValue )
    {
        // Cache systemSettingsCache = TODO SystemSettingsCache.getInstance();
        String key = section + "." + param;
        String ret = null; // TODO ( String )systemSettingsCache.get( key );
        if( MISSING_SETTING_VALUE.equals( ret ) )
        {
            return defValue;
        }
        if( ret != null )
        {
            return ret;
        }
        try
        {
            String sql = "SELECT setting_value FROM systemSettings WHERE setting_name = '" + param + "'" +
                    " AND section_name =" + safestr( connector, section, true );
            ret = null;//TODO = new JDBCRecordAdapterAsQuery( connector, sql ).getString();
            // TODO systemSettingsCache.put( key, ret );
            return ret;
        }
//        catch( JDBCRecordAdapterAsQuery.NoRecord ignore )
//        {
//            systemSettingsCache.put( key, MISSING_SETTING_VALUE );
//            return defValue;
//        }
        catch( Exception e )
        {
            String details = " Section: " + section + ", setting_name: " + param;
            log.log(Level.SEVERE, "Could not read system setting from DB. " + details, e);

            //TODO systemSettingsCache.put( key, MISSING_SETTING_VALUE );
            return defValue;
        }
    }

    private static ClassLoader systemClassLoader = Utils.class.getClassLoader();

    public static void setClassLoader(ClassLoader cl)
    {
        systemClassLoader = cl;
    }

    public static ClassLoader getClassLoader()
    {
        return systemClassLoader;
    }


}
