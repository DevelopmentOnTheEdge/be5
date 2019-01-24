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
        //response.setCharacterEncoding(encoding);

        if (download)
        {
            response.setHeader("Content-disposition", "attachment; filename=" +
                    UrlEscapers.urlFormParameterEscaper().escape(filename));
        }
        else
        {
            response.setHeader("Content-disposition", "filename=" +
                    UrlEscapers.urlFormParameterEscaper().escape(filename));
        }

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
}
