package com.developmentontheedge.be5.api.impl;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.xml.Jaxb;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class ResponseImpl implements Response
{

    /**
     * Must not be public.
     * @author asko
     */
    static class TypedResponse {
        final String type;
        final Object value;
        
        TypedResponse(String type, Object value)
        {
            this.type = type;
            this.value = value;
        }
    }
    
    /**
     * Must not be public.
     * @author asko
     */
    static class UntypedResponse {
        final Object value;
        
        UntypedResponse(Object value)
        {
            this.value = value;
        }
    }
    
    /**
     * Must not be public.
     * @author asko
     */
    static class Error {
        final String message;
        final String code;

        Error(String message, String code)
        {
            this.message = message;
            this.code = code;
        }
    }
    
    /**
     * Guarantees correct state of the response.
     */
    private final RawResponseWrapper response;
    
    public ResponseImpl(HttpServletResponse rawResponse)
    {
        this.response = new RawResponseWrapper(rawResponse);
    }

    @Override
    public void sendSuccess()
    {
        sendAsRawJson(typed("ok", null));
    }
    
    @Override
    public void sendAsJson(String type, Object value)
    {
        sendAsRawJson(typed(type, value));
    }
    
    @Override
    public void sendAsJson(Object value)
    {
        sendAsRawJson(untyped(value));
    }
    
    @Override
    public void sendError(Be5Exception e)
    {
        String msg = UserInfoHolder.isAdmin() ? e.getMessage() : "";
        //TODO localize e.getCode()
        sendAsJson("error", new Error(msg, e.getCode().toString()));
    }
        
    @Override
    public void sendAsRawJson(Object value)
    {
        sendJson(new GsonBuilder().disableHtmlEscaping().create().toJson(value));
    }
    
    @Override
    public void sendAsRawJson(JsonElement value)
    {
        sendJson(new Gson().toJson(value));
    }

    @Override
    public void sendJson(String json)
    {
        // The MIME media type for JSON text is 'application/json'.
        // The default encoding is UTF-8. Source: RFC 4627, http://www.ietf.org/rfc/rfc4627.txt.
        sendText("application/json", json);
    }
    
    @Override
    public <T> void sendAsXml(Class<T> klass, T object)
    {
        sendXml(new Jaxb().toXml(klass, object));
    }
    
    @Override
    public void sendXml(String xml)
    {
        // text/xml or application/xml
        // RFC 2376, http://www.ietf.org/rfc/rfc2376.txt
        sendText("application/xml", xml);
    }
    
    @Override
    public void sendUnknownActionError()
    {
        sendError("Unknown component action.", "UNKNOWN_ACTION");
    }
    
    private void sendText(String contentType, String text)
    {
        // The MIME media type for JSON text is 'application/json'.
        // The default encoding is UTF-8. Source: RFC 4627, http://www.ietf.org/rfc/rfc4627.txt.
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.append(text);
        response.flush();
    }

    private TypedResponse typed(String type, Object value)
    {
        return new TypedResponse(type, value);
    }
    
    private UntypedResponse untyped(Object value)
    {
        return new UntypedResponse(value);
    }

    @Override
    public void sendError(String message)
    {
        sendAsJson("error", message);
    }
    
    @Override
    public void sendError(String message, String code)
    {
        sendAsJson("error", new Error(message, code));
    }
    
    @Override
    public void sendTextError(String messagee)
    {
        sendText("text/plain", messagee);
    }
    
    @Override
    public HttpServletResponse getRawResponse()
    {
        return response.getRawResponse();
    }

}
