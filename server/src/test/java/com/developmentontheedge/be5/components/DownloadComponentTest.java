package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;

import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class DownloadComponentTest extends Be5ProjectTest
{
    @Inject private Injector injector;
    private Component component;

    @Before
    public void init()
    {
        component = (Component)injector.getComponent("download");
    }

    @Test
    @Ignore
    public void test()
    {
        Response response = mock(Response.class);
        Request req = getSpyMockRequest("", ImmutableMap.<String, String>builder()
            .put("_t_", "attachments")
            .put("_typeColumn_", "mimeType")
            .put("_charsetColumn_", "mimeCharset")
            .put("_filenameColumn_", "name")
            .put("_dataColumn_", "data")
            .put("_download_", "no")
            .put("ID", "7326")
            .build());

        component.generate(req, response, injector);
    }


}