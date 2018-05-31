package com.developmentontheedge.be5.web;

import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.web.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.web.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.web.model.jsonapi.ResourceData;
import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * <p>The main interface for sending responses.</p>
 * 
 * <p>If the client sent an incorrect route to the backend ({@link Request#getRequestUri()} returns an unexpected value), then
 * you must send the corresponding response with {@link Response#sendUnknownActionError()}.</p>
 */
public interface Response
{
    void sendAsJson(JsonApiModel jsonApiModel);

    void sendAsJson(ResourceData data, Object meta);

    void sendAsJson(ResourceData data, ResourceData[] included, Object meta);

    void sendAsJson(ResourceData data, ResourceData[] included, Object meta, Map<String, String> links);

    void sendErrorAsJson(ErrorModel error, Object meta);

    void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta);

    void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta, Map<String, String> links);

    //void sendErrorsAsJson(Object[] errors, Object meta);

//    /**
//     * <p>The conventional way to send untyped responses. It is recommended to use typed responses.</p>
//     *
//     * @param value will be serialized
//     */
//    void sendErrorsAsJson(Object value);

    void sendAsRawJson(Object value);

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

    default void sendFile(boolean download, String filename, String contentType, String charset, InputStream in)
    {
        HttpServletResponse response = getRawResponse();

        response.setContentType(contentType + "; charset=" + charset);
        //response.setCharacterEncoding(encoding);

        if (download)
        {
            response.setHeader("Content-disposition","attachment; filename=" + UrlEscapers.urlFormParameterEscaper().escape(filename));
        }
        else
        {
            response.setHeader("Content-disposition","filename=" + UrlEscapers.urlFormParameterEscaper().escape(filename));
        }

        try
        {
            ByteStreams.copy(in, response.getOutputStream());
            in.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
