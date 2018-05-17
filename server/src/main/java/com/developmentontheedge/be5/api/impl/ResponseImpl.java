package com.developmentontheedge.be5.api.impl;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletResponse;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.util.Jaxb;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.exceptions.Be5Exception;

import java.util.Map;
import java.util.logging.Logger;


public class ResponseImpl implements Response
{
    private static final Logger log = Logger.getLogger(ResponseImpl.class.getName());

    private static final Jsonb jsonb = JsonbBuilder.create();

    /**
     * Guarantees correct state of the response.
     */
    private final RawResponseWrapper response;
    
    public ResponseImpl(HttpServletResponse rawResponse)
    {
        this.response = new RawResponseWrapper(rawResponse);
    }

    /**
     * New json api? see JsonApiModel, ResourceData
     */
    @Override
    public void sendAsJson(JsonApiModel jsonApiModel)
    {
        sendAsRawJson(jsonApiModel);
    }

    @Override
    public void sendAsJson(ResourceData data, Object meta)
    {
        sendAsRawJson(JsonApiModel.data(data, meta));
    }

    @Override
    public void sendAsJson(ResourceData data, ResourceData[] included, Object meta)
    {
        sendAsRawJson(JsonApiModel.data(data, included, meta));
    }

    @Override
    public void sendAsJson(ResourceData data, ResourceData[] included, Object meta, Map<String, String> links)
    {
        sendAsRawJson(JsonApiModel.data(data, included, meta, links));
    }

    @Override
    public void sendErrorAsJson(ErrorModel error, Object meta)
    {
        //todo use HttpServletResponse.SC_INTERNAL_SERVER_ERROR (comment for prevent frontend errors)
        sendAsRawJson(JsonApiModel.error(error, meta));
    }

    @Override
    public void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta)
    {
        sendAsRawJson(JsonApiModel.error(error, included, meta));
    }

    @Override
    public void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta, Map<String, String> links)
    {
        sendAsRawJson(JsonApiModel.error(error, included, meta, links));
    }

//    @Override
//    public void sendErrorsAsJson(Object[] errors, Object meta)
//    {
//        throw new RuntimeException("");
//        //TODO create ErrorObject, sendAsRawJson(new JsonApiModel(errors, meta, links));
//    }

    @Override
    public void sendError(Be5Exception e)
    {
        ErrorModel errorModel;
        if(UserInfoHolder.isSystemDeveloper())
        {
            errorModel = new ErrorModel(e);
        }
        else
        {
            errorModel = new ErrorModel(e.getHttpStatusCode(), "");
        }

        sendErrorAsJson(errorModel, null);
    }

    @Override
    public void sendAsRawJson(Object value)
    {
        sendJson(jsonb.toJson(value));
    }

    @Override
    public void sendError(Object value, int status)
    {
        setStatus(status);
        sendJson(jsonb.toJson(value));
    }

    @Override
    public void setStatus(int status)
    {
        response.setStatus(status);
    }

    @Override
    public void sendJson(String json)
    {
        // The MIME media type for JSON text is 'application/json'.
        // The default encoding is UTF-8. Source: RFC 4627, http://www.ietf.org/rfc/rfc4627.txt.
        sendText("application/json;charset=UTF-8", json);
    }

    @Override
    public void sendHtml(String content)
    {
        sendText("text/html;charset=UTF-8", content);
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
        sendText("application/xml;charset=UTF-8", xml);
    }
    
    @Override
    public void sendUnknownActionError()
    {
        sendErrorAsJson( new ErrorModel("404", "Unknown component action."), null);
    }
    
    private void sendText(String contentType, String text)
    {
        // The MIME media type for JSON text is 'application/json'.
        // The default encoding is UTF-8. Source: RFC 4627, http://www.ietf.org/rfc/rfc4627.txt.
        response.setContentType(contentType);
        //response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.append(text);
        response.flush();
    }

    @Override
    public void sendTextError(String message)
    {
        sendText("text/plain;charset=UTF-8", message);
    }
    
    @Override
    public HttpServletResponse getRawResponse()
    {
        return response.getRawResponse();
    }

}
