package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.model.TablePresentation;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DocumentTest extends AbstractProjectTest
{
    private static Component component;

    @BeforeClass
    public static void init(){
        component = ServerModules.getComponent("document");
    }

    @Test
    public void generate() throws Exception {
        Response response = mock(Response.class);

        component.generate(getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.QUERY,"All records")), response, sp);

        verify(response).sendAsJson(eq("table"), any(TablePresentation.class));
    }

}