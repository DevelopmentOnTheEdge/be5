package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StaticPageComponentTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = ServerModules.getComponent("static");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);

        component.generate(getMockRequest("info.be"), response, sp);
        verify(response).sendAsJson(eq("static"), eq("<h1>Info</h1><p>Test text.</p>"));
    }

    @Test
    public void generateFoo() throws Exception
    {
        Response response = mock(Response.class);

        String page = "foo.be";
        component.generate(getMockRequest(page), response, sp);

        verify(response).sendError(eq(Be5ErrorCode.NOT_FOUND.exception(page)));
    }

}