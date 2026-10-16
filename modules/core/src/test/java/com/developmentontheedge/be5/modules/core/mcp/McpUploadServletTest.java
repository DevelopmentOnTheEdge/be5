package com.developmentontheedge.be5.modules.core.mcp;

import org.apache.commons.fileupload.FileItem;
import org.junit.After;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class McpUploadServletTest
{
    private static final String BOUNDARY = "----be5boundary";

    private final McpFileStore store = new McpFileStore();
    private final McpUploadServlet servlet = new McpUploadServlet(store);

    @After
    public void tearDown()
    {
        System.clearProperty(McpFileStore.MAX_SIZE_PROPERTY);
    }

    @Test
    public void pageToChooseTheFile() throws Exception
    {
        McpFileStore.Entry entry = store.create("anna", "Signed <contract> & annex");
        HttpMock http = new HttpMock("GET", "/api/mcp/upload/" + entry.token);

        servlet.doGet(http.request, http.response);

        assertEquals(200, http.status);
        assertTrue(http.body(), http.body().contains("Signed &lt;contract&gt; &amp; annex"));
        assertTrue(http.body(), http.body().contains("enctype=\"multipart/form-data\""));
        assertEquals("no-referrer", http.headers.get("Referrer-Policy"));
        assertEquals("no-store", http.headers.get("Cache-Control"));
        assertEquals("DENY", http.headers.get("X-Frame-Options"));

        HttpMock unknown = new HttpMock("GET", "/api/mcp/upload/unknown");
        servlet.doGet(unknown.request, unknown.response);
        assertEquals(404, unknown.status);
    }

    @Test
    public void uploadOnce() throws Exception
    {
        McpFileStore.Entry entry = store.create("anna", null);
        HttpMock http = multipart(entry.token, "C:\\fakepath\\scan.png", "image/png", "PNG data");

        servlet.doPost(http.request, http.response);

        assertEquals(http.body(), 200, http.status);
        assertTrue(http.body(), http.body().contains("\"fileId\":\"" + entry.fileId + "\""));
        assertTrue(http.body(), http.body().contains("\"name\":\"scan.png\""));
        FileItem file = store.get(entry.fileId, "anna");
        assertEquals("scan.png", file.getName());
        assertEquals("image/png", file.getContentType());
        assertEquals("PNG data", new String(file.get(), StandardCharsets.UTF_8));

        HttpMock again = multipart(entry.token, "other.png", "image/png", "other");
        servlet.doPost(again.request, again.response);
        assertEquals(404, again.status);
        assertEquals("PNG data", new String(store.get(entry.fileId, "anna").get(), StandardCharsets.UTF_8));
    }

    @Test
    public void uploadFromThePage() throws Exception
    {
        McpFileStore.Entry entry = store.create("anna", null);
        HttpMock http = multipart(entry.token, "report.pdf", "application/pdf", "%PDF").header("Accept", "text/html");

        servlet.doPost(http.request, http.response);

        assertEquals(200, http.status);
        assertTrue(http.body(), http.body().contains("<b>report.pdf</b>"));
        assertTrue(http.body(), http.body().contains("go back to the conversation"));
    }

    @Test
    public void wrongUploads() throws Exception
    {
        McpFileStore.Entry entry = store.create("anna", null);

        HttpMock notMultipart = new HttpMock("POST", "/api/mcp/upload/" + entry.token);
        when(notMultipart.request.getContentType()).thenReturn("application/json");
        servlet.doPost(notMultipart.request, notMultipart.response);
        assertEquals(400, notMultipart.status);
        assertTrue(notMultipart.body(), notMultipart.body().contains("curl -F"));

        System.setProperty(McpFileStore.MAX_SIZE_PROPERTY, "4");
        HttpMock tooLarge = multipart(entry.token, "big.bin", "application/octet-stream", "12345678");
        servlet.doPost(tooLarge.request, tooLarge.response);
        assertEquals(413, tooLarge.status);

        assertFalse("the link still works", store.forUpload(entry.token) == null);
        assertNull(store.forUpload(entry.token).getFile());
    }

    private static HttpMock multipart(String token, String fileName, String contentType, String content)
            throws Exception
    {
        String body = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n"
                + content + "\r\n"
                + "--" + BOUNDARY + "--\r\n";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpMock http = new HttpMock("POST", "/api/mcp/upload/" + token);
        when(http.request.getContentType()).thenReturn("multipart/form-data; boundary=" + BOUNDARY);
        when(http.request.getContentLength()).thenReturn(bytes.length);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        when(http.request.getInputStream()).thenReturn(new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return in.read();
            }
        });
        return http;
    }
}
