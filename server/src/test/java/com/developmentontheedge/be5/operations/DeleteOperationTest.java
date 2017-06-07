package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteOperationTest extends AbstractProjectTest{

    private OperationService operationService = sp.get(OperationService.class);

    @Test
    public void generateTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        Either<FormPresentation, OperationResult> generate = operationService.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Delete",
                RestApiConstants.SELECTED_ROWS, "1",
                RestApiConstants.VALUES, "")));

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"), generate.getSecond());

        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void executeTestOperation(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        OperationResult operationResult = operationService.execute(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, "testtableAdmin",
                RestApiConstants.QUERY, "All records",
                RestApiConstants.OPERATION, "Delete",
                RestApiConstants.SELECTED_ROWS, "1",
                RestApiConstants.VALUES, "")));

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"), operationResult);


        initUserWithRoles(RoleType.ROLE_GUEST);
    }

}