package com.developmentontheedge.be5.web.impl;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUploadWrapperTest
{
    @Test
    public void filesGivenToTheRequestAreFoundAsUploaded() throws IOException
    {
        HttpServletRequest raw = requestWithAttributes();
        RequestImpl request = new RequestImpl(raw);
        assertSame(raw, request.getRawRequest());

        FileItem report = file("report.pdf", "%PDF");
        FileItem scan1 = file("scan1.png", "png1");
        FileItem scan2 = file("scan2.png", "png2");
        Map<String, List<FileItem>> files = new HashMap<>();
        files.put("report", Collections.singletonList(report));
        files.put("scans", Arrays.asList(scan1, scan2));
        raw.setAttribute(FileUploadWrapper.ATTRIBUTE, new FileUploadWrapper(raw, files));

        FileUploadWrapper wrapper = (FileUploadWrapper) request.getRawRequest();
        assertSame(report, wrapper.getFileItem("report.pdf"));
        assertSame(scan2, wrapper.getFileItem("scan2.png"));
        assertEquals("report.pdf", wrapper.getParameter("report"));
        assertArrayEquals(new String[]{"scan1.png", "scan2.png"}, wrapper.getParameterValues("scans"));
        assertNull(wrapper.getFileItem("other.txt"));

        raw.removeAttribute(FileUploadWrapper.ATTRIBUTE);
        assertSame(raw, request.getRawRequest());
    }

    private static FileItem file(String name, String content) throws IOException
    {
        FileItem item = new DiskFileItemFactory().createItem("file", "application/octet-stream", false, name);
        try (OutputStream out = item.getOutputStream())
        {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return item;
    }

    private static HttpServletRequest requestWithAttributes()
    {
        Map<String, Object> attributes = new HashMap<>();
        HttpServletRequest raw = mock(HttpServletRequest.class);
        when(raw.getAttribute(anyString())).thenAnswer(inv -> attributes.get((String) inv.getArguments()[0]));
        doAnswer(inv -> attributes.put((String) inv.getArguments()[0], inv.getArguments()[1]))
                .when(raw).setAttribute(anyString(), org.mockito.Matchers.any());
        doAnswer(inv -> attributes.remove((String) inv.getArguments()[0])).when(raw).removeAttribute(anyString());
        return raw;
    }
}
