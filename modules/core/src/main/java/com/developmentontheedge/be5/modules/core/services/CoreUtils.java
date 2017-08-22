package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.util.BlobUtils;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CoreUtils extends com.developmentontheedge.be5.metadata.Utils
{
    private final String MISSING_SETTING_VALUE = "some-absolutely-impossble-setting-value";

    private final SqlService db;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final Injector injector;

    public CoreUtils(SqlService db, Meta meta, UserAwareMeta userAwareMeta, Injector injector)
    {
        this.db = db;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.injector = injector;
    }

    /**
     * Retrieving system settings parameter value for specified section and parameter. If there isn't such parameter, or
     * executing query throws any exception, then method will return defValue.
     * <br/>Results of method call are cached.
     *
     *
     * @param section system settings section name
     * @param param parameter name
     * @param defValue default value for return, if there isn't such section or parameter
     * @return section parameter value
     */
    // it is deliberately not synchronized!
    // it is better to let 2 processes to do the same thing twice than
    // to block on network call
    public String getSystemSettingInSection( String section, String param, String defValue )
    {
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

        String sql = "SELECT setting_value FROM systemSettings WHERE setting_name = ? AND section_name = ?";
        Clob clob = db.getScalar(sql, param, section);
        if(clob != null)
        {
            return BlobUtils.getAsString(clob);
        }
        else
        {
            return defValue;
        }
    }
//
//    /**
//     * Set system settings parameter to the specified value, and saves it to the DB.
//     *
//     * All of the parameters (section, param, value) must be already passed through the method
//     *
//     * @param section parameters section
//     * @param param parameter name
//     * @param value parameter value
//     * @throws SQLException
//     */
//    public void setSystemSettingInSection( String section, String param, String value ) throws SQLException
//    {
//        String queryUpdate =
//                "UPDATE systemSettings SET setting_value = " + safestr( value, true ) +
//                        " WHERE section_name=" + safestr( section, true ) +
//                        "   AND setting_name = " + safestr( param, true );
//        if ( 0 == connector.executeUpdate( queryUpdate ) )
//        {
//            String queryInsert = "INSERT INTO systemSettings( section_name, setting_name, setting_value ) VALUES ( "
//                    + safestr( section, true ) + ", "
//                    + safestr( param, true ) + ", "
//                    + safestr( value, true ) + " )";
//            connector.executeUpdate( queryInsert );
//        }
//        String key = section + "." + param;
//        SystemSettingsCache.getInstance().put( key, value );
//        SystemSettings.notifyListeners( section, param );
//        SystemSettings.notifyOtherHosts( section, param );
//    }
//
//    /**
//     * Get all settings for given section.
//     *
//     * In this method parameter section is passing through the method {@link #safestr(DatabaseString) safestr}
//     *
//     * @param connector
//     * @param section
//     * @return Map in the form parameter name - parameter value
//     * @throws SQLException
//     */
//    public Map getSystemSettingsInSection(String section ) throws SQLException
//    {
//        String sql = "SELECT setting_name, setting_value ";
//        sql += " FROM systemSettings WHERE section_name = '" + Utils.safestr( section ) + "'";
//        Map settingsInSection = readAsMap( sql );
//        for(Iterator iter = settingsInSection.entrySet().iterator(); iter.hasNext(); )
//        {
//            Map.Entry entry = ( Map.Entry )iter.next();
//            String key = section + "." + entry.getKey();
//            SystemSettingsCache.getInstance().put( key, entry.getValue() );
//        }
//        return settingsInSection;
//    }
//
    /**
     * Takes parameter param from the section "system", using the method with 4 parameters
     * {@link #setSystemSettingInSection(String, String) setSystemSettingInSection}(String, String)
     * Detaiiled information about what passes through {@link #safestr(DatabaseString) safestr}, describes in
     * {@link #setSystemSettingInSection(String, String) setSystemSettingInSection} method.
     *
     * @param param parameter name
     * @return
     */
    public String getSystemSetting( String param )
    {
        return getSystemSettingInSection( "system", param, null );
    }

    /**
     * Takes parameter param from the section "system", using the method with 4 parameters
     * {@link #setSystemSettingInSection(String, String) setSystemSettingInSection}(String, String)
     * Detaiiled information about what passes through {@link #safestr(DatabaseString) safestr}, describes in
     * {@link #setSystemSettingInSection(String, String) setSystemSettingInSection} method.
     *
     * @param connector
     * @param param parameter name
     * @param defValue this value is returned, when such parameter does not exists in DB
     * @return
     */
    public String getSystemSetting( String param, String defValue )
    {
        return getSystemSettingInSection( "system", param, defValue );
    }

    public boolean getBooleanSystemSetting( String param, boolean defValue )
    {
        String check = getSystemSetting( param, null );
        if( check == null )
        {
            return defValue;
        }
        return Arrays.asList( "TRUE", "YES", "1", "ON" ).contains( check.toUpperCase() );
    }

    public boolean getBooleanSystemSetting( String param )
    {
        return getBooleanSystemSetting( param, false );
    }

    /**
     * Takes parameter param from the section "module + '_module'", using the method with 4 parameters
     * {@link #getModuleSetting(String, String) getModuleSetting}(String, String),
     * where defValue is null
     * {@link #setSystemSettingInSection(String, String) setSystemSettingInSection} method.
     *
     * @param module module name
     * @param param parameter name
     * @return
     */
    public String getModuleSetting( String module, String param )
    {
        return getModuleSetting( module, param, null );
    }

    /**
     * Takes parameter param from the section "module + '_module'", using the method with 4 parameters
     * {@link #getModuleSetting(String, String) getModuleSetting}(String, String)
     * {@link #setSystemSettingInSection( String, String, String) setSystemSettingInSection} method.
     *
     * @param module
     * @param param
     * @param defValue
     * @return
     */
    public String getModuleSetting( String module, String param, String defValue )
    {
        return getSystemSettingInSection( module.toUpperCase() + "_module", param, defValue );
    }

//    public <T> T getModuleSettingByType( String module, String param, T defValue, Class<T> clazz )
//    {
//        String val = getModuleSetting(module, param);
//        if( val == null )
//            return defValue;
//        return changeType( val, clazz );
//    }

    public boolean getBooleanModuleSetting( String module, String param, boolean defValue )
    {
        String check = getModuleSetting( module, param, null );
        if( check == null )
        {
            return defValue;
        }
        return Arrays.asList( "TRUE", "YES", "1", "ON" ).contains( check.toUpperCase() );
    }

    public boolean getBooleanModuleSetting( String module, String param )
    {
        return getBooleanModuleSetting( module, param, false );
    }
//
//    /**
//     * Checks, if specified module is linked to this web site.
//     *
//     * @param connector DB connector
//     * @param module module name (case insensitive)
//     * @return true, if module is linked.
//     */
//    public boolean hasModule( String module )
//    {
//        String modules = getSystemSetting( "MODULES", "" );
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
//    public boolean hasFeatureFromCache( String feature )
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
//    public boolean hasFeature( String feature )
//    {
//        Collection features;
//        String setting = getSystemSetting( Features.SETTING_NAME );
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
//                setSystemSettingInSection( Features.SECTION_NAME, Features.SETTING_NAME, StringUtils.join( features, "," ) );
//            }
//            catch( SQLException e )
//            {
//                Logger.error( cat, "Cannot put FEATURES into database", e );
//            }
//        }
//        return features.contains( feature );
//    }
//
//    public final String SQL_PREF_START = "SELECT pref_value FROM user_prefs WHERE pref_name = ";
//
//    /**
//     * Retrieves specific user parameter from table user_prefs.
//     * <b>Attention!!!</b>Before table to be queried,user name is passing through {@link #safestr(DatabaseString)}. And
//     * parameter name does not passing method {@link #safestr(DatabaseString)}
//     *
//     * @param connector
//     * @param user
//     * @param param
//     * @return
//     */
//    public String getUserSetting( String user, String param )
//    {
//        if ( user == null )
//            return null;
//
//        String realUser = safestr( user, true );
//        try
//        {
//            String sql = SQL_PREF_START + safestr( param, true ) + " AND user_name = " + realUser;
//            return QRec.withCache( sql, UserSettingsCache.getInstance() ).getString();
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
//     * Set`s up specified user parameter. All of the parameters are passing through {@link #safestr(DatabaseString)}
//     * Some of the parameters are passed to {@link #removeUserSetting(String)}
//     *
//     * @param connector DB connector
//     * @param user user name
//     * @param param parameter name
//     * @param value parameter value
//     * @throws Exception
//     */
//
//    public void setUserSetting( String user, String param, String value ) throws Exception
//    {
//        setUserSetting( user, param, value, false );
//    }
//
//    public void setUserSetting( String user, String param, String value, boolean isQueued ) throws Exception
//    {
//        String realUser = safestr( user, true );
//
//        String cacheSql = SQL_PREF_START + safestr( param, true ) + " AND user_name = " + realUser;
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
//        final String sql = "UPDATE user_prefs SET pref_value = " + safestr( value, true ) +
//                " WHERE pref_name = " + safestr( param, true ) + " AND user_name = " + realUser;
//
//        final String sql2 = "INSERT INTO user_prefs VALUES( " + realUser + ", " + safestr( param, true ) + ", " + safestr( value, true ) + " )";
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
//     * <b>Attention!!!</b> Parameter param doesn't passing through {@link #safestr(DatabaseString)}
//     *
//     * @param connector
//     * @param user
//     * @param param
//     * @throws Exception
//     */
//    public void removeUserSetting( String user, String param ) throws Exception
//    {
//        String realUser = safestr( user, true );
//        String sql = "DELETE FROM user_prefs WHERE pref_name = " + safestr( param, true ) + " AND user_name = " + realUser;
//        connector.executeUpdate( sql );
//
//        String cacheSql = SQL_PREF_START + safestr( param, true ) + " AND user_name = " + realUser;
//        Cache cache = UserSettingsCache.getInstance();
//        cache.remove( cacheSql );
//    }
}
