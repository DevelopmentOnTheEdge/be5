package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import com.developmentontheedge.be5.components.ScriptList.ActionPaths;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptListTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = loadedClasses.get("scriptList");
    }

    @Test
    public void generate() throws Exception
    {
        Response response = mock(Response.class);
        Request req = getMockRequest("");
        when(req.getServletContextRealPath("be5/scripts/be5/actions"))
                .thenReturn("src/test/resources/be5/scripts/be5/actions");

        when(req.getServletContextRealPath("scripts/actions"))
                .thenReturn("src/test/resources/scripts/actions/");

        when(req.get("category")).thenReturn("scripts");

        component.generate(req, response, sp);
        List<ActionPaths> actionPathss = Arrays.asList(
                new ActionPaths("appAction", "actions/appAction"),
                new ActionPaths("test", "be5:be5/actions/test"),
                new ActionPaths("test2", "be5:be5/actions/test2")
        );

        verify(response).sendAsRawJson(eq(actionPathss));
    }

}