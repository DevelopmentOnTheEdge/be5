package com.developmentontheedge.be5.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;


public interface Response
{
    /**
     * @param value object for send as json
     */
    void sendAsJson(Object value);

    /**
     * @param value  object for send as json
     * @param status {@link javax.servlet.http.HttpServletResponse}
     */
    void sendAsJson(Object value, int status);

    /**
     * @param status {@link javax.servlet.http.HttpServletResponse}
     */
    void setStatus(int status);

    void sendJson(String json);

    void sendHtml(String json);

    void sendXml(String xml);

    void sendYaml(String yaml);

    HttpServletResponse getRawResponse();

    void redirect(String location);

    void addCookie(Cookie cookie);

    OutputStream getOutputStream() throws IOException;
}
