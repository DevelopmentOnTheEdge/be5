package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
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
                .map(tagValue -> getDps(ImmutableMap.of("CODE", tagValue, "Name", tagValue))).collect(Collectors.toList());

        when(SqlServiceMock.mock.selectList(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }
}
