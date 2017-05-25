package com.developmentontheedge.be5.api;

import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * <p>The main interface for sending responses.</p>
 * 
 * <p>Our general convention is to send JSON with two fields, <code>type</code> and <code>value</code>,
 * so usually you have to use {@link #sendAsJson(String, Object)} to send <code>application/json</code> response with these two field.
 * The given object will be serialized to JSON according the Gson serializarion rules.</p>
 * 
 * <p>If you JSON method doesn't return a sensible value, it should send the conventional success response with {@link Response#sendSuccess()}.</p>
 * 
 * <p>You JSON method can signal about an occured error with {@link Response#sendError(Be5Exception)},
 * {@link Response#sendError(String)} or {@link Response#sendError(String, String)}.</p>
 * 
 * <p>If the client sent an incorrect route to the backend ({@link Request#getRequestUri()} returns an unexpected value), then
 * you must send the corresponding response with {@link Response#sendUnknownActionError()}.</p>
 * 
 * @author asko
 */
public interface Response
{
    
    /**
     * <p>The conventional way to send typed responses.</p>
     * 
     * <p>Sends a JSON content with two fields, <code>type</code> and <code>value</code>.
     * The <code>type</code> is the passed string, the <code>value</code> is the result of serialization of the given value object with Gson.</p>
     * 
     * @param type type of response
     * @param value will be serialized with Gson,
     *        see <a href="https://sites.google.com/site/gson/gson-user-guide">the Gson documentation</a> for more information.
     * @see Gson#toJson(Object)
     */
    void sendAsJson(String type, Object value);
    
    /**
     * <p>The conventional way to send untyped responses. It is recommended to use typed responses.</p>
     *
     * @param value will be serialized with Gson,
     *        see <a href="https://sites.google.com/site/gson/gson-user-guide">the Gson documentation</a> for more information.
     * @see Gson#toJson(Object)
     * @see Response#sendAsJson(String, Object)
     */
    void sendAsJson(Object value);

    void sendBeanAsJson(String type, Object value);

    void sendBeanAsJson(Object value);
    
//    /**
//     * A way to send a pure response. Should be used only to support frontend components that we can't edit, e.g. DataTables jQuery plugin.
//     *
//     * @param value will be serialized with Gson,
//     *        see <a href="https://sites.google.com/site/gson/gson-user-guide">the Gson documentation</a> for more information.
//     * @see Gson#toJson(Object)
//     */
    void sendAsRawJson(Object value);
    
//    /**
//     * A way to send a pure response. Should be used only to support frontend components that we can't edit, e.g. DataTables jQuery plugin.
//     *
//     * @param value will be serialized with Gson,
//     *        see <a href="https://sites.google.com/site/gson/gson-user-guide">the Gson documentation</a> for more information.
//     * @see Gson#toJson(Object)
//     */
    //void sendAsRawJson(JsonElement value);
    
    /**
     * Sends a serialized json. It is recommended to use {@link Response#sendAsJson(String, Object)} or  {@link Response#sendAsJson(Object)} instead.
     */
    void sendJson(String json);

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

    /**
     * The conventional way to send the trivial response.
     */
    void sendSuccess();
    
    /**
     * The conventional way to respond when the {@link Request#getRequestUri()} returns an unexpected value.
     */
    void sendUnknownActionError();
    
    /**
     * A way to report about an occured error.
     */
    void sendError(Be5Exception e);

    /**
     * A way to report about an occured error.
     */
    void sendError(String message);
    
    /**
     * A way to report about an occured error.
     */
    void sendError(String message, String code);
    
    /**
     * A way to report about an occured error;
     */
    void sendTextError(String messagee);

    /**
     * Returns a raw response. Used only for low-level API. Should not be used in ordinary components.
     */
    HttpServletResponse getRawResponse();
    
}
