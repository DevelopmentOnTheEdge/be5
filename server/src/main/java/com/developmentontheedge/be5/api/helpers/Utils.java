package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Injector;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils extends com.developmentontheedge.be5.metadata.Utils
{
    private final SqlService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final Injector injector;

    public Utils(SqlService db, Meta meta, UserAwareMeta userAwareMeta, Injector injector)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.injector = injector;
    }

//    /**
//     * Retrieving system settings parameter value for specified section and parameter. If there isn't such parameter, or
//     * executing query throws any exception, then method will return defValue.
//     * <br/>Results of method call are cached.
//     *
//     * <b>Attention!!! In this method:</b>
//     * <br/> - parameter section is processing by {@link #safestr(DatabaseConnector, String) safestr}
//     * <br/> - parameter param is not processing by {@link #safestr(DatabaseConnector, String) safestr}
//     *
//     * @param connector DB connector
//     * @param section system settings section name
//     * @param param parameter name
//     * @param defValue default value for return, if there isn't such section or parameter
//     * @return section parameter value
//     */
//    // it is deliberately not synchronized!
//    // it is better to let 2 processes to do the same thing twice than
//    // to block on network call
//    public static String getSystemSettingInSection( DatabaseConnector connector, String section, String param, String defValue )
//    {
//        Cache systemSettingsCache = SystemSettingsCache.getInstance();
//        String key = section + "." + param;
//        String ret = ( String )systemSettingsCache.get( key );
//        if( MISSING_SETTING_VALUE.equals( ret ) )
//        {
//            return defValue;
//        }
//        if( ret != null )
//        {
//            return ret;
//        }
//        try
//        {
//            String sql = "SELECT setting_value FROM systemSettings WHERE setting_name = '" + param + "'" +
//                    " AND section_name =" + safestr( connector, section, true );
//            ret = new JDBCRecordAdapterAsQuery( connector, sql ).getString();
//            systemSettingsCache.put( key, ret );
//            return ret;
//        }
//        catch( JDBCRecordAdapterAsQuery.NoRecord ignore )
//        {
//            systemSettingsCache.put( key, MISSING_SETTING_VALUE );
//            return defValue;
//        }
//        catch( Exception e )
//        {
//            String details = " Section: " + section + ", setting_name: " + param;
//            Logger.error( cat, "Could not read system setting from DB. " + details, e );
//            systemSettingsCache.put( key, MISSING_SETTING_VALUE );
//            return defValue;
//        }
//    }
//
//    /**
//     * Set system settings parameter to the specified value, and saves it to the DB.
//     *
//     * All of the parameters (section, param, value) must be already passed through the method
//     * {@link #safestr(DatabaseConnector, String) safestr}
//     *
//     * @param connector DB connector
//     * @param section parameters section
//     * @param param parameter name
//     * @param value parameter value
//     * @throws SQLException
//     */
//    public static void setSystemSettingInSection( DatabaseConnector connector, String section, String param, String value ) throws SQLException
//    {
//        String queryUpdate =
//                "UPDATE systemSettings SET setting_value = " + safestr( connector, value, true ) +
//                        " WHERE section_name=" + safestr( connector, section, true ) +
//                        "   AND setting_name = " + safestr( connector, param, true );
//        if ( 0 == connector.executeUpdate( queryUpdate ) )
//        {
//            String queryInsert = "INSERT INTO systemSettings( section_name, setting_name, setting_value ) VALUES ( "
//                    + safestr( connector, section, true ) + ", "
//                    + safestr( connector, param, true ) + ", "
//                    + safestr( connector, value, true ) + " )";
//            connector.executeUpdate( queryInsert );
//        }
//        String key = section + "." + param;
//        SystemSettingsCache.getInstance().put( key, value );
//        SystemSettings.notifyListeners( connector, section, param );
//        SystemSettings.notifyOtherHosts( section, param );
//    }
//
//    /**
//     * Get all settings for given section.
//     *
//     * In this method parameter section is passing through the method {@link #safestr(DatabaseConnector, String) safestr}
//     *
//     * @param connector
//     * @param section
//     * @return Map in the form parameter name - parameter value
//     * @throws SQLException
//     */
//    public static Map getSystemSettingsInSection(DatabaseConnector connector, String section ) throws SQLException
//    {
//        String sql = "SELECT setting_name, setting_value ";
//        sql += " FROM systemSettings WHERE section_name = '" + Utils.safestr( connector, section ) + "'";
//        Map settingsInSection = readAsMap( connector, sql );
//        for(Iterator iter = settingsInSection.entrySet().iterator(); iter.hasNext(); )
//        {
//            Map.Entry entry = ( Map.Entry )iter.next();
//            String key = section + "." + entry.getKey();
//            SystemSettingsCache.getInstance().put( key, entry.getValue() );
//        }
//        return settingsInSection;
//    }
//
//    /**
//     * Takes parameter param from the section "system", using the method with 4 parameters
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection}(DatabaseConnector, String, String, String)
//     * Detaiiled information about what passes through {@link #safestr(DatabaseConnector, String) safestr}, describes in
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection} method.
//     *
//     * @param connector
//     * @param param parameter name
//     * @return
//     */
//    public static String getSystemSetting( DatabaseConnector connector, String param )
//    {
//        return getSystemSettingInSection( connector, "system", param, null );
//    }
//
//    /**
//     * Takes parameter param from the section "system", using the method with 4 parameters
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection}(DatabaseConnector, String, String, String)
//     * Detaiiled information about what passes through {@link #safestr(DatabaseConnector, String) safestr}, describes in
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection} method.
//     *
//     * @param connector
//     * @param param parameter name
//     * @param defValue this value is returned, when such parameter does not exists in DB
//     * @return
//     */
//    public static String getSystemSetting( DatabaseConnector connector, String param, String defValue )
//    {
//        return getSystemSettingInSection( connector, "system", param, defValue );
//    }
//
//    public static boolean getBooleanSystemSetting( DatabaseConnector connector, String param, boolean defValue )
//    {
//        String check = getSystemSetting( connector, param, null );
//        if( check == null )
//        {
//            return defValue;
//        }
//        return Arrays.asList( "TRUE", "YES", "1", "ON" ).contains( check.toUpperCase() );
//    }
//
//    public static boolean getBooleanSystemSetting( DatabaseConnector connector, String param )
//    {
//        return getBooleanSystemSetting( connector, param, false );
//    }
//
//    /**
//     * Takes parameter param from the section "module + '_module'", using the method with 4 parameters
//     * {@link #getModuleSetting(DatabaseConnector, String, String, String) getModuleSetting}(DatabaseConnector, String, String, String),
//     * where defValue is null
//     * Detailed information about what passes through {@link #safestr(DatabaseConnector, String) safestr}, describes in
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection} method.
//     *
//     * @param connector DB connector
//     * @param module module name
//     * @param param parameter name
//     * @return
//     */
//    public static String getModuleSetting( DatabaseConnector connector, String module, String param )
//    {
//        return getModuleSetting( connector, module, param, null );
//    }
//
//    /**
//     * Takes parameter param from the section "module + '_module'", using the method with 4 parameters
//     * {@link #getModuleSetting(DatabaseConnector, String, String, String) getModuleSetting}(DatabaseConnector, String, String, String)
//     * Detailed information about what passes through {@link #safestr(DatabaseConnector, String) safestr}, describes in
//     * {@link #setSystemSettingInSection(DatabaseConnector, String, String, String) setSystemSettingInSection} method.
//     *
//     * @param connector DB connector
//     * @param module
//     * @param param
//     * @param defValue
//     * @return
//     */
//    public static String getModuleSetting( DatabaseConnector connector, String module, String param, String defValue )
//    {
//        return getSystemSettingInSection( connector, module.toUpperCase() + "_module", param, defValue );
//    }
//
//    public static <T> T getModuleSettingByType( DatabaseConnector connector, String module, String param, T defValue, Class<T> clazz )
//    {
//        String val = getModuleSetting(connector, module, param);
//        if( val == null )
//            return defValue;
//        return changeType( val, clazz );
//    }
//
//    public static boolean getBooleanModuleSetting( DatabaseConnector connector, String module, String param, boolean defValue )
//    {
//        String check = getModuleSetting( connector, module, param, null );
//        if( check == null )
//        {
//            return defValue;
//        }
//        return Arrays.asList( "TRUE", "YES", "1", "ON" ).contains( check.toUpperCase() );
//    }
//
//    public static boolean getBooleanModuleSetting( DatabaseConnector connector, String module, String param )
//    {
//        return getBooleanModuleSetting( connector, module, param, false );
//    }
//
//    /**
//     * Checks, if specified module is linked to this web site.
//     *
//     * @param connector DB connector
//     * @param module module name (case insensitive)
//     * @return true, if module is linked.
//     */
//    public static boolean hasModule( DatabaseConnector connector, String module )
//    {
//        String modules = getSystemSetting( connector, "MODULES", "" );
//        List modList = Arrays.asList( modules.split( "," ) );
//        return modList.contains( module.toLowerCase() ) || modList.contains( module.toUpperCase() );
//    }
//
//    /**
//     * Checks whether the specified feature is enabled.
//     *
//     * @param feature Feature name.
//     * @return true if so, false otherwise.
//     */
//
//    public static boolean hasFeatureFromCache( String feature )
//    {
//        Cache systemSettingsCache = SystemSettingsCache.getInstance();
//        String key = "system." + Features.SETTING_NAME;
//        String setting = ( String )systemSettingsCache.get( key );
//        if( setting == null || MISSING_SETTING_VALUE.equals( setting ) )
//        {
//            return false;
//        }
//        Collection features = Arrays.asList( setting.split( "," ) );
//        return features.contains( feature );
//    }
//
//    public static boolean hasFeature( DatabaseConnector connector, String feature )
//    {
//        Collection features;
//        String setting = getSystemSetting( connector, Features.SETTING_NAME );
//        if( null != setting )
//        {
//            features = Arrays.asList( setting.split( "," ) );
//        }
//        else
//        {
//            // This is needed for backward compatibility only.
//            // Old projects may not have FEATURES in systemSettings,
//            // so we add it automatically.
//            features = Features.guessByTableExistence( connector );
//            try
//            {
//                setSystemSettingInSection( connector, Features.SECTION_NAME, Features.SETTING_NAME, StringUtils.join( features, "," ) );
//            }
//            catch( SQLException e )
//            {
//                Logger.error( cat, "Cannot put FEATURES into database", e );
//            }
//        }
//        return features.contains( feature );
//    }
//
//    public static final String SQL_PREF_START = "SELECT pref_value FROM user_prefs WHERE pref_name = ";
//
//    /**
//     * Retrieves specific user parameter from table user_prefs.
//     * <b>Attention!!!</b>Before table to be queried,user name is passing through {@link #safestr(DatabaseConnector, String)}. And
//     * parameter name does not passing method {@link #safestr(DatabaseConnector, String)}
//     *
//     * @param connector
//     * @param user
//     * @param param
//     * @return
//     */
//    public static String getUserSetting( DatabaseConnector connector, String user, String param )
//    {
//        if ( user == null )
//            return null;
//
//        String realUser = safestr( connector, user, true );
//        try
//        {
//            String sql = SQL_PREF_START + safestr( connector, param, true ) + " AND user_name = " + realUser;
//            return QRec.withCache( connector, sql, UserSettingsCache.getInstance() ).getString();
//        }
//        catch( QRec.NoRecord exc )
//        {
//            return null;
//        }
//        catch( Exception exc )
//        {
//            Logger.error( cat, "When getting user pref", exc );
//            return null;
//        }
//    }
//
//    /**
//     * Set`s up specified user parameter. All of the parameters are passing through {@link #safestr(DatabaseConnector, String)}
//     * Some of the parameters are passed to {@link #removeUserSetting(DatabaseConnector, String, String)}
//     *
//     * @param connector DB connector
//     * @param user user name
//     * @param param parameter name
//     * @param value parameter value
//     * @throws Exception
//     */
//
//    public static void setUserSetting( DatabaseConnector connector, String user, String param, String value ) throws Exception
//    {
//        setUserSetting( connector, user, param, value, false );
//    }
//
//    public static void setUserSetting( DatabaseConnector connector, String user, String param, String value, boolean isQueued ) throws Exception
//    {
//        String realUser = safestr( connector, user, true );
//
//        String cacheSql = SQL_PREF_START + safestr( connector, param, true ) + " AND user_name = " + realUser;
//
//        Cache cache = UserSettingsCache.getInstance();
//
//        QRec prev = ( QRec )cache.get( cacheSql );
//        if( prev != null && !prev.isEmpty() && value != null && value.equals( prev.getString() ) )
//        {
//            return;
//        }
//
//        cache.put( cacheSql, new QRec( "pref_value", value ) );
//
//        final String sql = "UPDATE user_prefs SET pref_value = " + safestr( connector, value, true ) +
//                " WHERE pref_name = " + safestr( connector, param, true ) + " AND user_name = " + realUser;
//
//        final String sql2 = "INSERT INTO user_prefs VALUES( " + realUser + ", " + safestr( connector, param, true ) + ", " + safestr( connector, value, true ) + " )";
//
//        if( !isQueued )
//        {
//            if( connector.executeUpdate( sql ) == 0 )
//            {
//                connector.executeUpdate( sql2 );
//            }
//            return;
//        }
//
//        try
//        {
//            Utils.queuedUpdate( sql, new QueuedStatement.Callback()
//            {
//                @Override
//                public void run( Object result )
//                {
//                    if( ( ( Integer )result ) != 0 )
//                    {
//                        return;
//                    }
//
//                    Utils.queuedUpdate( sql2 );
//                }
//            } );
//        }
//        catch( QueuedStatementExecuterIsDownException exc )
//        {
//            if( connector.executeUpdate( sql ) == 0 )
//            {
//                connector.executeUpdate( sql2 );
//            }
//        }
//    }
//
//    /**
//     * Removes specified user settings parameter.
//     * <b>Attention!!!</b> Parameter param doesn't passing through {@link #safestr(DatabaseConnector, String)}
//     *
//     * @param connector
//     * @param user
//     * @param param
//     * @throws Exception
//     */
//    public static void removeUserSetting( DatabaseConnector connector, String user, String param ) throws Exception
//    {
//        String realUser = safestr( connector, user, true );
//        String sql = "DELETE FROM user_prefs WHERE pref_name = " + safestr( connector, param, true ) + " AND user_name = " + realUser;
//        connector.executeUpdate( sql );
//
//        String cacheSql = SQL_PREF_START + safestr( connector, param, true ) + " AND user_name = " + realUser;
//        Cache cache = UserSettingsCache.getInstance();
//        cache.remove( cacheSql );
//    }
}
