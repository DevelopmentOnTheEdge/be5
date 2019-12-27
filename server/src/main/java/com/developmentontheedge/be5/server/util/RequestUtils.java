package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.web.Response;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class RequestUtils
{
    public static void sendFile(Response res, String filename, String contentType, InputStream in)
    {
        sendFile(res, false, filename, contentType, StandardCharsets.UTF_8.name(), in);
    }

    public static void sendFile(Response res, boolean download, String filename, String contentType, String charset,
                                InputStream in)
    {
        HttpServletResponse response = res.getRawResponse();

        response.setContentType(contentType + "; charset=" + charset);
        response.setHeader("Content-disposition", (download ? "attachment; " : "") + "filename*=UTF-8''"
                + parseFileName(filename));

        try
        {
            ByteStreams.copy(in, res.getOutputStream());
            in.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String parseFileName(String filename)
    {
        String newFileName = UrlEscapers.urlFormParameterEscaper().escape(filename);
        newFileName = newFileName.replaceAll("\\+", "%20");
        System.out.println("newFileName=" + newFileName);
        return newFileName;
    }

}
