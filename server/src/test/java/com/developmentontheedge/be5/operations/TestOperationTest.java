package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestOperationTest extends AbstractProjectTest{

    private OperationService operationService = sp.get(OperationService.class);

    @Test
    public void generateTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String values = new Gson().toJson(ImmutableMap.of(
                "name","test1",
                "value", "test2"));

        Either<FormPresentation, OperationResult> generate = operationService.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "TestOperation",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        FormPresentation form = generate.getFirst();

        assertEquals("TestOperation", form.title);

        assertEquals("{'name':'testName','number':1}",
                oneQuotes(form.bean.getJsonObject("values").toString()));

        assertEquals("Name",
                form.bean.getJsonObject("meta").getJsonObject("/name").getString("displayName"));

        assertEquals("Number",
                form.bean.getJsonObject("meta").getJsonObject("/number").getString("displayName"));

        assertEquals("['/name','/number']",
                oneQuotes(form.bean.getJsonArray("order").toString()));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void executeTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        String values = new Gson().toJson(ImmutableMap.of(
                "name","test1",
                "value", "test2"));

        OperationResult operationResult = operationService.execute(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "TestOperation",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        assertNotNull(operationResult);

//        assertEquals(OperationResult.redirect(
//                new HashUrl(FrontendConstants.TABLE_ACTION,"testtableAdmin", "All records")
//                            .named(ImmutableMap.of("name","test1", "value","test2"))
//        ), operationResult);

//        assertEquals((Long)1L, db.getScalar(
//                "SELECT COUNT(*) FROM testtableAdmin WHERE name = ? AND value = ?", name, value));

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}