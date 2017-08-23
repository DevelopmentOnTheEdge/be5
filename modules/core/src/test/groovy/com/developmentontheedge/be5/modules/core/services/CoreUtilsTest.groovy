package com.developmentontheedge.be5.modules.core.services

import com.developmentontheedge.be5.api.services.CacheInfo
import com.developmentontheedge.be5.api.services.SqlService
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class CoreUtilsTest extends AbstractProjectIntegrationH2Test
{
    DatabaseModel database = injector.get(DatabaseModel.class)
    SqlService db = injector.getSqlService()

    CoreUtils utils = injector.get(CoreUtils.class)

    //todo Inject annotation
    //@Inject CoreUtils utils

    @Before
    void before(){
        db.update("DELETE FROM systemSettings")
        db.update("DELETE FROM user_prefs")
        CacheInfo.clearAll()
    }

    @Test
    void getSystemSettingInSection() throws Exception
    {
        database.systemSettings << [ section_name: "system", setting_name: "app_name", setting_value: "Test App" ]
        assertEquals "Test App", utils.
                getSystemSettingInSection("system", "app_name", "Be5 Application")
    }

    @Test
    void getSystemSettingInSectionNotFound() throws Exception
    {
        assertEquals "Be5 Application", utils.
                getSystemSettingInSection("system", "app_name", "Be5 Application")
    }

    @Test
    void getSystemSettingNotFound() throws Exception
    {
        assertEquals null, utils.getSystemSetting("app_name")
        assertEquals "No value", utils.getSystemSetting("app_name", "No value")
    }

    @Test
    void setSystemSettingInSection() throws Exception
    {
        utils.setSystemSettingInSection("system", "app_name", "Name 1")
        assertEquals "Name 1", utils.getSystemSetting("app_name")

        utils.setSystemSettingInSection("system", "app_name", "Name 2")
        assertEquals "Name 2", utils.getSystemSetting("app_name")
    }

    @Test
    void getSystemSettingsInSectionTest() throws Exception
    {
        utils.setSystemSettingInSection("system", "app_name", "App")
        utils.setSystemSettingInSection("system", "app_url", "Url")

        assertEquals "[app_name:App, app_url:Url]",
                utils.getSystemSettingsInSection("system").toString()

        assertEquals "App", utils.getSystemSetting("app_name")
        assertEquals "Url", utils.getSystemSetting("app_url")
    }

    @Test
    void getBooleanSystemSetting() throws Exception
    {
        assertEquals false, utils.getBooleanSystemSetting("is_active")
        assertEquals CoreUtils.MISSING_SETTING_VALUE,
                CacheInfo.getCache("System settings").getIfPresent("system.is_active")
        assertEquals true, utils.getBooleanSystemSetting("is_active", true)

        database.systemSettings << [ section_name: "system", setting_name: "is_active", setting_value: "true" ]
        CacheInfo.clearAll()

        assertEquals true, utils.getBooleanSystemSetting("is_active")
    }

    @Test
    void getModuleSetting() throws Exception
    {
        assertEquals false, utils.getBooleanModuleSetting("core", "is_active")
        assertEquals true, utils.getBooleanModuleSetting("core", "is_active", true)

        assertEquals null, utils.getModuleSetting("core", "is_active")
        assertEquals "false", utils.getModuleSetting("core", "is_active", "false")

        database.systemSettings << [ section_name: "CORE_module", setting_name: "is_active", setting_value: "true" ]
        CacheInfo.clearAll()

        assertEquals true, utils.getBooleanModuleSetting("core", "is_active")
    }

    @Test
    void getUserSetting() throws Exception
    {
        assertEquals null, utils.getUserSetting("testName", "companyID")
        assertEquals CoreUtils.MISSING_SETTING_VALUE,
                CacheInfo.getCache("User settings").getIfPresent("testName.companyID")

        assertEquals null, utils.getUserSetting("testName", "companyID")

        database.user_prefs << [ user_name: "testName", pref_name: "companyID", pref_value: "123" ]
        CacheInfo.clearAll()

        assertEquals "123", utils.getUserSetting("testName", "companyID")

        utils.removeUserSetting("testName", "companyID")

        assertEquals null, utils.getUserSetting("testName", "companyID")
    }

    @Test
    void setUserSettingTest() throws Exception
    {
        utils.setUserSetting("testName", "companyID", "1")
        assertEquals "1", utils.getUserSetting("testName", "companyID")

        utils.setUserSetting("testName", "companyID", "2")
        assertEquals "2", utils.getUserSetting("testName", "companyID")
    }

    @Test(expected = NullPointerException)
    void getBooleanModuleSettingNull() throws Exception
    {
        assertEquals "error", utils.getBooleanModuleSetting("test", null)
    }

    @Test(expected = NullPointerException)
    void getBooleanModuleSettingNull2() throws Exception
    {
        assertEquals "error", utils.getBooleanModuleSetting(null, "test")
    }

    @Test(expected = NullPointerException)
    void getUserSettingNullParams() throws Exception {
        assertEquals null, utils.getUserSetting(null, "test")
    }

    @Test(expected = NullPointerException)
    void getUserSettingNullParams2() throws Exception {
        assertEquals null, utils.getUserSetting("test", null)
    }
}