package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FormTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = injector.getComponent("form");
    }

    @Test
    public void generate() throws Exception {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Response response = mock(Response.class);
        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));
        component.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtableAdmin",
                RestApiConstants.QUERY,"All records",
                RestApiConstants.OPERATION,"Insert",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)), response, sp);

        verify(response).sendAsJson(eq("form"), any(Either.class));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}