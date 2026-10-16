package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The test project has no _system_ entity (of the core module): the Query Builder is for system developers.
 */
public class QueryBuilderAccessTest extends ServerBe5ProjectTest
{
    @Inject
    private QueryBuilderAccess queryBuilderAccess;

    @After
    public void tearDown()
    {
        initGuest();
    }

    @Test
    public void systemDevelopersWithoutTheQueryBuilderQuery()
    {
        assertFalse(meta.hasEntity(QueryBuilderAccess.ENTITY_NAME));

        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
        assertTrue(queryBuilderAccess.isAllowed());

        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        assertFalse(queryBuilderAccess.isAllowed());

        initGuest();
        assertFalse(queryBuilderAccess.isAllowed());

        UserInfo user = UserInfoHolder.getLoggedUser();
        UserInfoHolder.setLoggedUser(null);
        try
        {
            assertFalse(queryBuilderAccess.isAllowed());
        }
        finally
        {
            UserInfoHolder.setLoggedUser(user);
        }
    }
}
