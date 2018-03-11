package com.developmentontheedge.be5.api;

import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;


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

    void sendErrorAsJson(ErrorModel error, Object meta);

    void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta);

    void sendErrorsAsJson(Object[] errors, Object meta);

//    /**
//     * <p>The conventional way to send untyped responses. It is recommended to use typed responses.</p>
//     *
//     * @param value will be serialized
//     */
//    void sendErrorsAsJson(Object value);

    void sendAsRawJson(Object value);

    void sendJson(String json);

    void sendHtml(String json);

    /**
     * Sends an XML response. See JAXB documentation to know about the serialization algorithm.
     * 
     * @param klass a class of the object to serialize
     * @param object an object to serialize
     */
    <T> void sendAsXml(Class<T> klass, T object);
    
    /**
     * Sends a serialized XML. It is recommended to use {@link Response#sendAsXml(Class, Object)} instead.
     */
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
    
    /**
     * A way to report about an occured error.
     */
    void sendError(Be5Exception e);
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
