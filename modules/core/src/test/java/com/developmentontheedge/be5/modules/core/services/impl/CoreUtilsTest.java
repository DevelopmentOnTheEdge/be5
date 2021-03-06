package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.cache.Be5Caches;
import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.EntityModel;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;

import static com.developmentontheedge.be5.modules.core.services.impl.CoreUtilsImpl.COLUMN_SETTINGS_ENTITY;
import static com.developmentontheedge.be5.modules.core.services.impl.CoreUtilsImpl.QUERY_SETTINGS_ENTITY;
import static com.google.inject.internal.util.ImmutableMap.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        db.update("DELETE FROM be5columnSettings");
        be5Caches.clearAll();
    }

    @Test
    public void getSystemSettingInSection()
    {
        database.getEntity("systemSettings").add(of(
                "section_name", "system",
                "setting_name", "app_name",
                "setting_value", "Test App"));
        assertEquals( "Test App", utils.
                getSystemSettingInSection("system", "app_name", "Be5 Application"));
    }

    @Test
    public void getSystemSettingInSectionNotFound()
    {
        assertEquals("Be5 Application", utils.getSystemSettingInSection("system", "app_name", "Be5 Application"));
    }

    @Test
    public void getSystemSettingNotFound()
    {
        assertNull(utils.getSystemSetting("app_name"));
        assertEquals("No value", utils.getSystemSetting("app_name", "No value"));
    }

    @Test
    public void setSystemSettingInSection()
    {
        utils.setSystemSettingInSection("system", "app_name", "Name 1");
        assertEquals("Name 1", utils.getSystemSetting("app_name"));

        utils.setSystemSettingInSection("system", "app_name", "Name 2");
        assertEquals("Name 2", utils.getSystemSetting("app_name"));
    }

    @Test
    public void getSystemSettingsInSectionTest()
    {
        utils.setSystemSettingInSection("system", "app_name", "App");
        utils.setSystemSettingInSection("system", "app_url", "Url");

        assertEquals("{app_name=App, app_url=Url}", utils.getSystemSettingsInSection("system").toString());

        assertEquals("App", utils.getSystemSetting("app_name"));
        assertEquals("Url", utils.getSystemSetting("app_url"));
    }

    @Test
    public void getBooleanSystemSetting()
    {
        assertFalse(utils.getBooleanSystemSetting("is_active"));
        assertEquals(CoreUtilsImpl.MISSING_SETTING_VALUE, be5Caches.getCache("System settings").getIfPresent("system.is_active"));
        assertTrue(utils.getBooleanSystemSetting("is_active", true));

        database.getEntity("systemSettings").add(of(
                "section_name", "system",
                "setting_name", "is_active",
                "setting_value", "true"));
        be5Caches.clearAll();

        assertTrue(utils.getBooleanSystemSetting("is_active"));
    }

    @Test
    public void getModuleSetting()
    {
        assertFalse(utils.getBooleanModuleSetting("core", "is_active"));
        assertTrue(utils.getBooleanModuleSetting("core", "is_active", true));

        assertNull(utils.getModuleSetting("core", "is_active"));
        assertEquals("false", utils.getModuleSetting("core", "is_active", "false"));

        database.getEntity("systemSettings").add(of(
                "section_name", "CORE_module",
                "setting_name", "is_active",
                "setting_value", "true"));
        be5Caches.clearAll();

        assertTrue(utils.getBooleanModuleSetting("core", "is_active"));
    }

    @Test
    public void getUserSetting()
    {
        assertNull(utils.getUserSetting("testName", "companyID"));
        assertEquals(CoreUtilsImpl.MISSING_SETTING_VALUE, be5Caches.getCache("User settings").getIfPresent("testName.companyID"));

        assertNull(utils.getUserSetting("testName", "companyID"));

        database.getEntity("user_prefs").add(of(
                "user_name", "testName",
                "pref_name", "companyID",
                "pref_value", "123"));
        be5Caches.clearAll();

        assertEquals("123", utils.getUserSetting("testName", "companyID"));

        utils.removeUserSetting("testName", "companyID");

        assertNull(utils.getUserSetting("testName", "companyID"));
    }

    @Test
    public void setUserSettingTest()
    {
        utils.setUserSetting("testName", "companyID", "1");
        assertEquals("1", utils.getUserSetting("testName", "companyID"));

        utils.setUserSetting("testName", "companyID", "2");
        assertEquals("2", utils.getUserSetting("testName", "companyID"));
    }

    @Test
    public void getColumnSettingForUserTest()
    {
        assertEquals(Collections.emptyMap(), utils.getColumnSettingForUser("users", "All records", "User", TEST_USER));

        database.getEntity(COLUMN_SETTINGS_ENTITY).add(new HashMap<String, Object>() {{
            put("table_name", "users");
            put("query_name", "All records");
            put("column_name", "User");
            put("user_name", TEST_USER);
            put("quick", "yes");
        }});
        be5Caches.clearAll();

        assertEquals("yes", utils.getColumnSettingForUser("users", "All records", "User", TEST_USER).get("quick"));

        utils.removeColumnSettingForUser("users", "All records", "User", TEST_USER);

        assertEquals(Collections.emptyMap(), utils.getColumnSettingForUser("users", "All records", "User", TEST_USER));
    }

    @Test
    public void setColumnSettingForUserTest()
    {
        EntityModel<Long> columnSettings = database.getEntity(COLUMN_SETTINGS_ENTITY);
        utils.setColumnSettingForUser("users", "All records", "User", TEST_USER,
                Collections.singletonMap("quick", "yes"));
        assertEquals("yes",
                utils.getColumnSettingForUser("users", "All records", "User", TEST_USER).get("quick"));
        assertEquals("yes", columnSettings.getBy(com.google.common.collect.ImmutableMap.of(
                "table_name", "users",
                "query_name", "All records",
                "column_name", "User")).getValueAsString("quick"));

        utils.setColumnSettingForUser("users", "All records", "User", TEST_USER,
                Collections.singletonMap("quick", "no"));
        assertEquals("no",
                utils.getColumnSettingForUser("users", "All records", "User", TEST_USER).get("quick"));
        assertEquals("no", columnSettings.getBy(com.google.common.collect.ImmutableMap.of(
                "table_name", "users",
                "query_name", "All records",
                "column_name", "User")).getValueAsString("quick"));
    }

    @Test
    public void getQuerySettingForUserTest()
    {
        assertEquals(Collections.emptyMap(), utils.getQuerySettingForUser("users", "All records", TEST_USER));

        database.getEntity(QUERY_SETTINGS_ENTITY).add(new HashMap<String, Object>() {{
            put("table_name", "users");
            put("query_name", "All records");
            put("user_name", TEST_USER);
            put("recordsPerPage", 100);
        }});

        be5Caches.clearAll();
        assertEquals(100, utils.getQuerySettingForUser("users", "All records", TEST_USER).get("recordsPerPage"));

        utils.removeQuerySettingForUser("users", "All records", TEST_USER);
        assertEquals(Collections.emptyMap(), utils.getQuerySettingForUser("users", "All records", TEST_USER));

        utils.setQuerySettingForUser("users", "All records", TEST_USER, of("recordsPerPage", 1000));
        assertEquals(1000, utils.getQuerySettingForUser("users", "All records", TEST_USER).get("recordsPerPage"));

        utils.setQuerySettingForUser("users", "All records", TEST_USER, of("recordsPerPage", 1001));
        assertEquals(1001, utils.getQuerySettingForUser("users", "All records", TEST_USER).get("recordsPerPage"));
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getBooleanModuleSettingNull()
    {
        utils.getBooleanModuleSetting("test", null);
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getBooleanModuleSettingNull2()
    {
        utils.getBooleanModuleSetting(null, "test");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getUserSettingNullParams()
    {
        utils.getUserSetting(null, "test");
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void getUserSettingNullParams2()
    {
        utils.getUserSetting("test", null);
    }
}
