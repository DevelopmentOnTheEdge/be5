package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import javax.inject.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;


public abstract class SqlMockOperationTest extends Be5ProjectTest
{
    @Inject protected DpsHelper dpsHelper;
    @Inject protected Meta meta;

    @Before
    public void beforeSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
        SqlServiceMock.clearMock();
    }

    @After
    public void afterSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    public static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getDpsS(ImmutableMap.of("CODE", tagValue, "Name", tagValue))).collect(Collectors.toList());

        when(SqlServiceMock.mock.selectList(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }
}
