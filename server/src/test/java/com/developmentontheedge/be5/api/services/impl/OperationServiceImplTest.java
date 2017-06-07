package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class OperationServiceImplTest extends AbstractProjectTest{

    @Test
    @Ignore
    public void generateInsert(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        OperationService operationService = sp.get(OperationService.class);

        String values = new Gson().toJson(ImmutableList.of(
                ImmutableMap.of("name","name",  "value","test1"),
                ImmutableMap.of("name","value", "value","test2")));

        Either<FormPresentation, OperationResult> generate = operationService.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Insert",
                RestApiConstants.SELECTED_ROWS, "0",
                RestApiConstants.VALUES, values)));

        FormPresentation form = generate.getFirst();

        assertEquals("{'name':'test1','value':'test2'}",
                oneQuotes(form.bean.getJsonObject("values").toString()));

        assertEquals("name",
                form.bean.getJsonObject("meta").getJsonObject("/name").getString("displayName"));

        assertEquals("value",
                form.bean.getJsonObject("meta").getJsonObject("/value").getString("displayName"));

        assertEquals("['/name','/value']",
                oneQuotes(form.bean.getJsonArray("order").toString()));

        assertEquals("Insert", form.title);

        initUserWithRoles(RoleType.ROLE_GUEST);
    }


}