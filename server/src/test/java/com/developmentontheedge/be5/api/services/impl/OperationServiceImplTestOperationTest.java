package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OperationServiceImplTestOperationTest extends AbstractProjectTest{

    private OperationService operationService = sp.get(OperationService.class);

    @Test
    public void generateTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));

        Either<FormPresentation, FrontendAction> generate = operationService.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "TestOperation",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        FormPresentation form = generate.getFirst();

        assertEquals("TestOperation", form.title);

        assertEquals("{'name':'testName','number':1}",
                oneQuotes(form.dps.getJsonObject("values").toString()));

        assertEquals("Name",
                form.dps.getJsonObject("meta").getJsonObject("/name").getString("displayName"));

        assertEquals("Number",
                form.dps.getJsonObject("meta").getJsonObject("/number").getString("displayName"));

        assertEquals("['/name','/number']",
                oneQuotes(form.dps.getJsonArray("order").toString()));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void executeTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));

        FrontendAction frontendAction = operationService.execute(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "TestOperation",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        assertEquals(FrontendAction.defaultAction(), frontendAction);

        assertNotNull(frontendAction);
//        assertEquals((Long)1L, db.getScalar(
//                "SELECT COUNT(*) FROM testtableAdmin WHERE name = ? AND value = ?", name, value));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}