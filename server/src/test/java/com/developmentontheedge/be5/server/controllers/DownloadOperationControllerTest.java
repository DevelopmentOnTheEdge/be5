package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.operations.support.DownloadOperationSupport;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static com.developmentontheedge.be5.server.RestApiConstants.CONTEXT_PARAMS;
import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY_NAME_PARAM;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DownloadOperationControllerTest extends ServerBe5ProjectTest
{
    @Inject
    private DownloadOperationController component;

    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
    }

    @Test
    public void controller()
    {
        Response response = mock(Response.class);
        Request req = getSpyMockRequest("/api/download/", ImmutableMap.<String, Object>builder()
                .put(ENTITY_NAME_PARAM, "testtable")
                .put(QUERY_NAME_PARAM, "All records")
                .put(OPERATION_NAME_PARAM, "TestDownloadOperation")
                .put(CONTEXT_PARAMS, "{}")
                .build());

        component.generate(req, response);
        verify(response).sendHtml("test");
    }

    @Test
    public void execute()
    {
        Either<Object, OperationResult> resultEither =
                executeOperation("testtable", "All records", "TestDownloadOperation", "");
        FrontendAction frontendAction = FrontendActions.downloadOperation(
                "testtable", "All records", "TestDownloadOperation",
                emptyMap(), emptyMap()
        );
        assertEquals(FrontendActions.closeMainModal(), ((FrontendAction[]) resultEither.getSecond().getDetails())[0]);
        assertEquals(frontendAction, ((FrontendAction[]) resultEither.getSecond().getDetails())[1]);
    }

    public static class TestDownloadOperation extends DownloadOperationSupport
    {
        @Override
        public void invokeWithResponse(Response res, Object parameters) throws Exception
        {
            res.sendHtml("test");
        }
    }
}
