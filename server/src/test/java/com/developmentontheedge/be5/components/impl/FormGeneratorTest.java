package com.developmentontheedge.be5.components.impl;

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
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class FormGeneratorTest extends AbstractProjectTest
{

    @Test
    public void insertTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        OperationService operationService = sp.get(OperationService.class);

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
    @Ignore
    public void insertTestTableAdmin(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        OperationService operationService = sp.get(OperationService.class);

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));

        Either<FormPresentation, FrontendAction> generate = operationService.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Insert",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        FormPresentation form = generate.getFirst();

        assertEquals("{'name':'test1','value':'test2'}",
                oneQuotes(form.dps.getJsonObject("values").toString()));

        assertEquals("name",
                form.dps.getJsonObject("meta").getJsonObject("/name").getString("displayName"));

        assertEquals("value",
                form.dps.getJsonObject("meta").getJsonObject("/value").getString("displayName"));

        assertEquals("['/name','/value']",
                oneQuotes(form.dps.getJsonArray("order").toString()));

        assertEquals("Insert", form.title);

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}