package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.AbstractProjectTestH2DB;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class OperationExecutorTest extends AbstractProjectTestH2DB
{
    private SqlService db = ServerModules.getServiceProvider().getSqlService();

    @Test
    public void testGenerate(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String name = "OExecutorTest1";
        String value = "OExecutorTest2";

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value",name),
                ImmutableMap.of("name","value", "value",value)));

        FrontendAction frontendAction = new OperationExecutor(ServerModules.getServiceProvider())
                .execute(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Insert",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        assertNotNull(frontendAction);
        assertEquals((Long)1L, db.getScalar(
                "SELECT COUNT(*) FROM testtableAdmin WHERE name = ? AND value = ?", name, value));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}