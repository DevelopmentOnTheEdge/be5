package com.developmentontheedge.be5.modules.core.mcp;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.test.mocks.TestRequest;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.impl.FileUploadWrapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Files for operations: inline in the values and uploaded by the link of upload_file, given to fields
 * of the type File (multipart, as the UI uploads) and Base64File.
 */
public class McpFileUploadTest extends CoreBe5ProjectDBTest
{
    @Inject
    private McpService mcpService;
    @Inject
    private McpFileStore fileStore;
    @Inject
    private Request request;

    private final HttpServletRequest raw = mock(HttpServletRequest.class);

    @Before
    public void init()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        // RequestImpl shows the files attached to the request as its raw request, the test request too
        when(TestRequest.mock.getRawRequest()).thenAnswer(inv -> {
            Object files = request.getAttribute(FileUploadWrapper.ATTRIBUTE);
            return files != null ? files : raw;
        });
        when(raw.getHeader("Host")).thenReturn("be5.example.org");
        when(raw.getContextPath()).thenReturn("");
    }

    @After
    public void tearDown()
    {
        TestRequest.newMock();
        System.clearProperty(McpFileStore.MAX_SIZE_PROPERTY);
        initGuest();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void formShowsFileFields()
    {
        Map<String, Object> form = (Map<String, Object>) call("get_operation_parameters", operation("UploadFile"));

        List<Map<String, Object>> fields = (List<Map<String, Object>>) form.get("fields");
        assertEquals(args("name", "document", "title", "Document", "type", "file", "required", true), fields.get(0));
        assertEquals("file", fields.get(1).get("type"));
        assertEquals(true, fields.get(1).get("multiple"));
        assertEquals("string", fields.get(2).get("type"));

        Map<String, Object> base64Form = (Map<String, Object>) call("get_operation_parameters",
                operation("UploadBase64File"));
        assertEquals("file", ((List<Map<String, Object>>) base64Form.get("fields")).get(0).get("type"));
    }

    @Test
    public void inlineFiles()
    {
        Map<String, Object> arguments = operation("UploadFile");
        arguments.put("values", args(
                "document", inline("note.txt", "text/plain", "hello"),
                "attachments", Arrays.asList(inline("a.txt", null, "A"),
                        args("name", "b.csv", "base64", "data:text/csv;base64," + base64("B"))),
                "comment", "two attachments"));

        assertEquals("note.txt text/plain hello + a.txt application/octet-stream A + b.csv text/csv B"
                + " / two attachments", message(call("execute_operation", arguments)));
        assertNull("the files are not left in the request", request.getAttribute(FileUploadWrapper.ATTRIBUTE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void uploadedFile() throws Exception
    {
        Map<String, Object> upload = (Map<String, Object>) call("upload_file", args("purpose", "The report"));
        String fileId = (String) upload.get("fileId");
        String url = (String) upload.get("uploadUrl");
        assertTrue(url, url.startsWith("https://be5.example.org/api/mcp/upload/"));
        assertTrue((String) upload.get("next"), ((String) upload.get("next")).contains("curl -F file=@<path> '" + url));

        Map<String, Object> arguments = operation("UploadFile");
        arguments.put("values", args("document", args("fileId", fileId)));
        assertTrue(callError("execute_operation", arguments).contains("is not uploaded yet"));

        McpFileStore.Entry entry = fileStore.forUpload(url.substring(url.lastIndexOf('/') + 1));
        assertEquals("The report", entry.purpose);
        fileStore.upload(entry, file("report.pdf", "application/pdf", "%PDF-1.4"));

        assertEquals("report.pdf application/pdf %PDF-1.4 / null", message(call("execute_operation", arguments)));
        assertEquals("the file can be used again", "report.pdf application/pdf %PDF-1.4 / null",
                message(call("execute_operation", arguments)));

        Map<String, Object> base64Arguments = operation("UploadBase64File");
        base64Arguments.put("values", args("picture", args("fileId", fileId)));
        assertEquals("report.pdf application/pdf %PDF-1.4", message(call("execute_operation", base64Arguments)));

        initUserWithNameAndRoles("anotherMcpUser", RoleType.ROLE_ADMINISTRATOR);
        assertTrue(callError("execute_operation", arguments).startsWith("Unknown or expired file"));
    }

    @Test
    public void base64FileField()
    {
        Map<String, Object> arguments = operation("UploadBase64File");
        arguments.put("values", args("picture", inline("pic.png", "image/png", "PNG")));

        assertEquals("pic.png image/png PNG", message(call("execute_operation", arguments)));
    }

    @Test
    public void wrongInlineFiles()
    {
        Map<String, Object> arguments = operation("UploadFile");
        arguments.put("values", args("document", args("name", "x.bin", "base64", "not base64!")));
        assertTrue(callError("execute_operation", arguments).contains("is not base64"));

        System.setProperty(McpFileStore.MAX_SIZE_PROPERTY, "4");
        arguments.put("values", args("document", inline("big.txt", "text/plain", "123456789")));
        assertTrue(callError("execute_operation", arguments).contains("upload it by a link from upload_file"));
    }

    private static Map<String, Object> operation(String operationName)
    {
        return args("entityName", "testtable", "queryName", "All records", "operationName", operationName);
    }

    private static Map<String, Object> inline(String name, String mimeType, String content)
    {
        Map<String, Object> file = args("name", name, "base64", base64(content));
        if (mimeType != null) file.put("mimeType", mimeType);
        return file;
    }

    private static String base64(String content)
    {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    private static FileItem file(String name, String contentType, String content) throws Exception
    {
        FileItem item = new DiskFileItemFactory().createItem("file", contentType, false, name);
        try (OutputStream out = item.getOutputStream())
        {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    private static String message(Object result)
    {
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(map.toString(), true, map.get("executed"));
        return (String) map.get("message");
    }

    @SuppressWarnings("unchecked")
    private Object call(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> result = callTool(tool, arguments);
        String text = (String) ((List<Map<String, Object>>) result.get("content")).get(0).get("text");
        assertEquals(text, false, result.get("isError"));
        return JsonUtils.getMapFromJson("{\"r\":" + text + "}").get("r");
    }

    @SuppressWarnings("unchecked")
    private String callError(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> result = callTool(tool, arguments);
        String text = (String) ((List<Map<String, Object>>) result.get("content")).get(0).get("text");
        assertEquals(text, true, result.get("isError"));
        return text;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callTool(String tool, Map<String, Object> arguments)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("name", tool);
        params.put("arguments", arguments);
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", "1");
        request.put("method", "tools/call");
        request.put("params", params);
        return (Map<String, Object>) mcpService.handleRequest(request).get("result");
    }

    private static Map<String, Object> args(Object... keysAndValues)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
