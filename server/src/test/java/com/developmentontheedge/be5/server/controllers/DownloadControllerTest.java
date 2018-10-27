package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DownloadControllerTest extends ServerBe5ProjectTest
{
    @Inject
    private DownloadController component;

    @Test
    public void test() throws IOException
    {
        when(DbServiceMock.mock.select(any(), any(), any())).thenReturn(getDpsS(ImmutableMap.of(
                "id", 1,
                "mimeType", "testMimeType",
                "name", "name",
                "data", "test data".getBytes()
        )));
        Response response = mock(Response.class);
        when(response.getRawResponse()).thenReturn(mock(HttpServletResponse.class));

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
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
        //verify(outputStream).write(eq("test data".getBytes()));
    }


}