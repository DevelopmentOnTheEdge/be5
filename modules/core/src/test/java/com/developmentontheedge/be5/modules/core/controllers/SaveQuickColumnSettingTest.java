package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.config.CoreUtils;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class SaveQuickColumnSettingTest extends CoreBe5ProjectDBTest
{
    @Inject
    private SaveQuickColumnSetting component;
    @Inject
    private CoreUtils coreUtils;

    @Test
    public void test()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        assertEquals("ok", component.generate(getSpyMockRequest("/api/quick", ImmutableMap.of(
                "table_name", "users",
                "query_name", "All records",
                "column_name", "User",
                "quick", "yes"
        ))));
        assertEquals("yes", coreUtils.getColumnSettingForUser(
                "users", "All records", "User", TEST_USER).get("quick"));

        assertEquals("ok", component.generate(getSpyMockRequest("/api/quick", ImmutableMap.of(
                "table_name", "users",
                "query_name", "All records",
                "column_name", "User",
                "quick", "no"
        ))));
        assertEquals("no", coreUtils.getColumnSettingForUser(
                "users", "All records", "User", TEST_USER).get("quick"));
    }
}
