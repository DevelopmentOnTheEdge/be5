package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.base.services.Be5Caches;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.modules.core.services.impl.CoreUtilsImpl;
import com.google.inject.internal.util.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class CoreUtilsTest extends CoreBe5ProjectDBTest
{
    @Inject
    private DatabaseModel database;
    @Inject
    private DbService db;
    @Inject
    private CoreUtils utils;
    @Inject
    private Be5Caches be5Caches;

    @Before
    public void before()
    {
        db.update("DELETE FROM systemSettings");
        db.update("DELETE FROM user_prefs");
        be5Caches.clearAll();
    }

    @Test
    public void getSystemSettingInSection() throws Exception
    {
        database.getEntity("systemSettings").add(ImmutableMap.of(
                "section_name", "system",
                "setting_name", "app_name",
                "setting_value", "Test App"));
        assertEquals( "Test App", utils.
                getSystemSettingInSection("system", "app_name", "Be5 Application"));
    }

    @Test
    public void getSystemSettingInSectionNotFound() throws Exception
    {
        assertEquals("Be5 Application", utils.getSystemSettingInSection("system", "app_name", "Be5 Application"));
    }

    @Test
    public void getSystemSettingNotFound() throws Exception
    {
        assertEquals(null, utils.getSystemSetting("app_name"));
        assertEquals("No value", utils.getSystemSetting("app_name", "No value"));
    }

    @Test
    public void setSystemSettingInSection() throws Exception
    {
        utils.setSystemSettingInSection("system", "app_name", "Name 1");
        assertEquals("Name 1", utils.getSystemSetting("app_name"));

        utils.setSystemSettingInSection("system", "app_name", "Name 2");
        assertEquals("Name 2", utils.getSystemSetting("app_name"));
    }

    @Test
    public void getSystemSettingsInSectionTest() throws Exception
    {
        utils.setSystemSettingInSection("system", "app_name", "App");
        utils.setSystemSettingInSection("system", "app_url", "Url");

        assertEquals("{app_name=App, app_url=Url}", utils.getSystemSettingsInSection("system").toString());

        assertEquals("App", utils.getSystemSetting("app_name"));
        assertEquals("Url", utils.getSystemSetting("app_url"));
    }

    @Test
    public void getBooleanSystemSetting() throws Exception
    {
        assertEquals(false, utils.getBooleanSystemSetting("is_active"));
        assertEquals(CoreUtilsImpl.MISSING_SETTING_VALUE, be5Caches.getCache("System settings").getIfPresent("system.is_active"));
        assertEquals(true, utils.getBooleanSystemSetting("is_active", true));

        database.getEntity("systemSettings").add(ImmutableMap.of(
                "section_name", "system",
                "setting_name", "is_active",
                "setting_value", "true"));
        be5Caches.clearAll();

        assertEquals(true, utils.getBooleanSystemSetting("is_active"));
    }

    @Test
    public void getModuleSetting() throws Exception
    {
        assertEquals(false, utils.getBooleanModuleSetting("core", "is_active"));
        assertEquals(true, utils.getBooleanModuleSetting("core", "is_active", true));

        assertEquals(null, utils.getModuleSetting("core", "is_active"));
        assertEquals("false", utils.getModuleSetting("core", "is_active", "false"));

        database.getEntity("systemSettings").add(ImmutableMap.of(
                "section_name", "CORE_module",
                "setting_name", "is_active",
                "setting_value", "true"));
        be5Caches.clearAll();

        assertEquals(true, utils.getBooleanModuleSetting("core", "is_active"));
    }

    @Test
    public void getUserSetting() throws Exception
    {
        assertEquals(null, utils.getUserSetting("testName", "companyID"));
        assertEquals(CoreUtilsImpl.MISSING_SETTING_VALUE, be5Caches.getCache("User settings").getIfPresent("testName.companyID"));

        assertEquals(null, utils.getUserSetting("testName", "companyID"));

        database.getEntity("user_prefs").add(ImmutableMap.of(
                "user_name", "testName",
                "pref_name", "companyID",
                "pref_value", "123"));
        be5Caches.clearAll();

        assertEquals("123", utils.getUserSetting("testName", "companyID"));

        utils.removeUserSetting("testName", "companyID");

        assertEquals(null, utils.getUserSetting("testName", "companyID"));
    }

    @Test
    public void setUserSettingTest() throws Exception
    {
        utils.setUserSetting("testName", "companyID", "1");
        assertEquals("1", utils.getUserSetting("testName", "companyID"));

        utils.setUserSetting("testName", "companyID", "2");
        assertEquals("2", utils.getUserSetting("testName", "companyID"));
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getBooleanModuleSettingNull() throws Exception
    {
        utils.getBooleanModuleSetting("test", null);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getBooleanModuleSettingNull2() throws Exception
    {
        utils.getBooleanModuleSetting(null, "test");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getUserSettingNullParams() throws Exception
    {
        utils.getUserSetting(null, "test");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getUserSettingNullParams2() throws Exception
    {
        utils.getUserSetting("test", null);
    }
}
