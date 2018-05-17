package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import javax.inject.Inject;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;


public class DownloadControllerTest extends ServerBe5ProjectTest
{
    @Inject private DownloadController component;

    @Test
    @Ignore
    public void test()
    {
        Response response = mock(Response.class);
        Request req = getSpyMockRequest("", ImmutableMap.<String, Object>builder()
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