package com.developmentontheedge.be5.modules.core.services

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
    void getBooleanSystemSetting() throws Exception
    {
        assertEquals false, utils.getBooleanSystemSetting("is_active")
        assertEquals true, utils.getBooleanSystemSetting("is_active", true)

        database.systemSettings << [ section_name: "system", setting_name: "is_active", setting_value: "true" ]

        assertEquals true, utils.getBooleanSystemSetting("is_active")
    }

    @Test
    void getModuleSetting() throws Exception
    {
        assertEquals false, utils.getBooleanModuleSetting("core", "is_active")
        assertEquals true, utils.getBooleanModuleSetting("core", "is_active", true)
        assertEquals null, utils.getModuleSetting("core", "is_active")

        database.systemSettings << [ section_name: "CORE_module", setting_name: "is_active", setting_value: "true" ]

        assertEquals true, utils.getBooleanModuleSetting("core", "is_active")
    }

    @Test
    void getUserSetting() throws Exception
    {
        assertEquals null, utils.getUserSetting("testName", "companyID")

        database.user_prefs << [ user_name: "testName", pref_name: "companyID", pref_value: "123" ]

        assertEquals "123", utils.getUserSetting("testName", "companyID")
    }
}