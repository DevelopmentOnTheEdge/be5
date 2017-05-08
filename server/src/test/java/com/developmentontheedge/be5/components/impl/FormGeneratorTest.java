package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class FormGeneratorTest extends AbstractProjectTest
{

    @Test
    @Ignore
    public void testGenerate(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));

        Either<FormPresentation, FrontendAction> generate = new FormGenerator().generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Insert",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        assertNotNull(generate);

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}