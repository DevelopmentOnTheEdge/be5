package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.ErrorMessages;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainServletTest extends AbstractProjectTest
{
    @Test
    public void runComponent() throws Exception
    {
        Request req = getMockRequest("info.be");
        Response res = mock(Response.class);

        new MainServlet().runComponent("static", req, res);

        verify(res).sendAsJson(eq("static"), eq("<h1>Info</h1><p>Test text.</p>"));
    }

    @Test
    public void unknownComponent() throws Exception
    {
        String componentId = "foo";
        Request req = getMockRequest("");
        Response res = mock(Response.class);

        new MainServlet().runComponent(componentId, req, res);

        res.sendError(eq(ErrorMessages.formatMessage(Be5ErrorCode.UNKNOWN_COMPONENT, componentId)));
    }

}