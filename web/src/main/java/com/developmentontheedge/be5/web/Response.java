package com.developmentontheedge.be5.web;

import javax.servlet.http.HttpServletResponse;


/**
 * <p>The main interface for sending responses.</p>
 */
public interface Response
{
    void sendAsJson(Object value);

    /**
     * @param value object for send as json
     * @param status {@link java.net.HttpURLConnection}
     */
    void sendError(Object value, int status);

    void setStatus(int status);

    void sendJson(String json);

    void sendHtml(String json);

    void sendXml(String xml);

//    /**
//     * The conventional way to send the trivial response.
//     */
//    void sendSuccess();
//
    /**
     * The conventional way to respond when the {@link Request#getRequestUri()} returns an unexpected value.
     */
    void sendUnknownActionError();

//    /**
//     * A way to report about an occured error.
//     */
//    void sendError(Be5Exception e);
//
//    /**
//     * A way to report about an occured error.
//     */
//    void sendError(String message);
//
//    /**
//     * A way to report about an occured error.
//     */
//    void sendError(String message, String code);
//
    /**
     * A way to report about an occured error;
     */
    void sendTextError(String messagee);

    /**
     * Returns a raw response. Used only for low-level API. Should not be used in ordinary components.
     */
    HttpServletResponse getRawResponse();
}
