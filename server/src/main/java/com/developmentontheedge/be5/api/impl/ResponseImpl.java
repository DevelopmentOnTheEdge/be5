package com.developmentontheedge.be5.api.impl;

import java.nio.charset.StandardCharsets;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.util.Jaxb;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

public class ResponseImpl implements Response
{
    private static final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withNullValues(true));

    public static class TypedResponse {
        final String type;
        final Object value;

        TypedResponse(String type, Object value)
        {
            this.type = type;
            this.value = value;
        }

        public String getType()
        {
            return type;
        }

        public Object getValue()
        {
            return value;
        }
    }

    public static class UntypedResponse {
        final Object value;

        UntypedResponse(Object value)
        {
            this.value = value;
        }

        public Object getValue()
        {
            return value;
        }
    }

    public static class ErrorResponse
    {
        final String message;
        final String code;

        ErrorResponse(String message, String code)
        {
            this.message = message;
            this.code = code;
        }

        public String getMessage()
        {
            return message;
        }

        public String getCode()
        {
            return code;
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
        sendAsJson("error", new ErrorResponse(msg, e.getCode().toString()));
    }
        
    @Override
    public void sendAsRawJson(Object value)
    {
        sendJson(jsonb.toJson(value));
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
        sendAsJson("error", new ErrorResponse(message, code));
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
