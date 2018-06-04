package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;


public class DownloadControllerTest extends ServerBe5ProjectTest
{
    @Inject private DownloadController component;

    @Test
    @Ignore
    public void test()
    {
        Response response = mock(Response.class);
        Request req = getSpyMockRequest("/api/download/", ImmutableMap.<String, Object>builder()
            .put("_t_", "attachments")
            .put("_typeColumn_", "mimeType")
            .put("_charsetColumn_", "mimeCharset")
            .put("_filenameColumn_", "name")
            .put("_dataColumn_", "data")
            .put("_download_", "no")
            .put("ID", "7326")
            .build());

        component.generate(req, response);
    }


}