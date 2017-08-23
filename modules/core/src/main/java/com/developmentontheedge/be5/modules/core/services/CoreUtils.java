package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.util.BlobUtils;

import java.util.Arrays;
import java.util.HashMap;
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
        Object value = db.getScalar(sql, param, section);
        if(value != null)
        {
            return BlobUtils.getAsString(value);
        }
        else
        {
            return defValue;
        }
    }

    /**
     * Set system settings parameter to the specified value, and saves it to the DB.
     *
     * All of the parameters (section, param, value) must be already passed through the method
     *
     * @param section system settings section name
     * @param param parameter name
     * @param value parameter value
     */
    public void setSystemSettingInSection( String section, String param, String value )
    {
        String queryUpdate = "UPDATE systemSettings SET setting_value = ?" +
                             " WHERE section_name= ? AND setting_name = ?";

        if ( 0 == db.update( queryUpdate, value, section, param) )
        {
            String queryInsert = "INSERT INTO systemSettings( section_name, setting_name, setting_value ) VALUES ( ?, ?, ?)";
            db.insert( queryInsert, section, param, value );
        }
//        String key = section + "." + param;
//        SystemSettingsCache.getInstance().put( key, value );

//        SystemSettings.notifyListeners( section, param );
//        SystemSettings.notifyOtherHosts( section, param );
    }

    /**
     * Get all settings for given section.
     *
     * @param section system settings section name
     * @return Map in the form parameter name - parameter value
     */
    public Map<String, String> getSystemSettingsInSection( String section )
    {
        String sql = "SELECT setting_name, setting_value FROM systemSettings WHERE section_name = ?";
        Map<String, String> settingsInSection = new HashMap<>();
        db.selectList(sql, rs ->
                settingsInSection.put(rs.getString(1), BlobUtils.getAsString(rs.getObject(2))),
            section);
        //Map<String, String> settingsInSection = readAsMap( sql );
//        for(Iterator iter = settingsInSection.entrySet().iterator(); iter.hasNext(); )
//        {
//            Map.Entry entry = ( Map.Entry )iter.next();
//            String key = section + "." + entry.getKey();
//            //SystemSettingsCache.getInstance().put( key, entry.getValue() );
//        }
        return settingsInSection;
    }

    /**
     * Takes parameter param from the section "system", using the method with 3 parameters
     * {@link #setSystemSettingInSection(String, String, String) setSystemSettingInSection} method.
     *
     * @param param parameter name
     * @return parameter value
     */
    public String getSystemSetting( String param )
    {
        return getSystemSettingInSection( "system", param, null );
    }

    /**
     * Takes parameter param from the section "system", using the method with 3 parameters
     * {@link #setSystemSettingInSection(String, String, String) setSystemSettingInSection} method.
     *
     * @param param parameter name
     * @param defValue this value is returned, when such parameter does not exists in DB
     * @return parameter value
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
     * Takes parameter param from the section "module + '_module'", using the method with 3 parameters
     * {@link #getModuleSetting(String, String, String) getModuleSetting}(String, String, String),
     * where defValue is null
     * {@link #setSystemSettingInSection(String, String, String) setSystemSettingInSection} method.
     *
     * @param module module name
     * @param param parameter name
     * @return module parameter value
     */
    public String getModuleSetting( String module, String param )
    {
        return getModuleSetting( module, param, null );
    }

    /**
     * Takes parameter param from the section "module + '_module'", using the method with 3 parameters
     * {@link #setSystemSettingInSection( String, String, String) setSystemSettingInSection} method.
     *
     * @param module module name
     * @param param parameter name
     * @param defValue default value for return, if there isn't such section or parameter
     * @return module parameter value
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

    /**
     * Retrieves specific user parameter from table user_prefs.
     *
     * @param user user name
     * @param param parameter name
     * @return parameter value
     */
    public String getUserSetting( String user, String param )
    {
        if ( user == null )
            return null;
        //QRec.withCache UserSettingsCache.getInstance()

        Object value = db.getScalar("SELECT pref_value FROM user_prefs WHERE pref_name = ? AND user_name = ?",
                param, user);
        if(value != null)
        {
            return BlobUtils.getAsString(value);
        }
        return null;
    }

    /**
     * Set`s up specified user parameter.
     * Some of the parameters are passed to {@link #removeUserSetting(String, String)}
     *
     * @param user user name
     * @param param parameter name
     * @param value parameter value
     */
    public void setUserSetting( String user, String param, String value )
    {
//        String cacheSql = SQL_PREF_START + " AND user_name = ?";
//        Cache cache = UserSettingsCache.getInstance();
//        QRec prev = ( QRec )cache.get( cacheSql );
//        if( prev != null && !prev.isEmpty() && value != null && value.equals( prev.getString() ) )
//        {
//            return;
//        }
        //cache.put( cacheSql, new QRec( "pref_value", value ) );

        final String sql =  "UPDATE user_prefs SET pref_value = ? WHERE pref_name = ? AND user_name = ?";
        final String sql2 = "INSERT INTO user_prefs VALUES( ?, ?, ? )";

        if( db.update( sql, value, param, user) == 0 )
        {
            db.insert( sql2, user, param, value );
        }
    }

    /**
     * Removes specified user settings parameter.
     *
     * @param user user name
     * @param param parameter name
     */
    public void removeUserSetting( String user, String param )
    {
        db.update("DELETE FROM user_prefs WHERE pref_name = ? AND user_name = ?", param, user);

        //String cacheSql = SQL_PREF_START + safestr( param, true ) + " AND user_name = " + realUser;
        //Cache cache = UserSettingsCache.getInstance();
        //cache.remove( cacheSql );
    }
}
