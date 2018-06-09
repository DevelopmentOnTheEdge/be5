package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;


public class JsonApiResponseHelper
{
    public final Logger log = Logger.getLogger(JsonApiResponseHelper.class.getName());

    private final UserInfoProvider userInfoProvider;
    private final Provider<Response> responseProvider;

    @Inject
    public JsonApiResponseHelper(UserInfoProvider userInfoProvider, Provider<Response> responseProvider)
    {
        this.userInfoProvider = userInfoProvider;
        this.responseProvider = responseProvider;
    }

    public void sendAsJson(JsonApiModel jsonApiModel)
    {
        responseProvider.get().sendAsJson(jsonApiModel);
    }

    public void sendAsJson(ResourceData data, Object meta)
    {
        responseProvider.get().sendAsJson(JsonApiModel.data(data, meta));
    }

    public void sendAsJson(ResourceData data, ResourceData[] included, Object meta)
    {
        responseProvider.get().sendAsJson(JsonApiModel.data(data, included, meta));
    }

    public void sendAsJson(ResourceData data, ResourceData[] included, Object meta, Map<String, String> links)
    {
        responseProvider.get().sendAsJson(JsonApiModel.data(data, included, meta, links));
    }

    public void sendErrorAsJson(ErrorModel error, Object meta)
    {
        //todo use HttpServletResponse.SC_INTERNAL_SERVER_ERROR (comment for prevent frontend errors)
        responseProvider.get().sendAsJson(JsonApiModel.error(error, meta));
    }

    public void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta)
    {
        responseProvider.get().sendAsJson(JsonApiModel.error(error, included, meta));
    }

    public void sendErrorAsJson(ErrorModel error, ResourceData[] included, Object meta, Map<String, String> links)
    {
        responseProvider.get().sendAsJson(JsonApiModel.error(error, included, meta, links));
    }

    public void sendUnknownActionError()
    {
        sendErrorAsJson( new ErrorModel("404", "Unknown component action."), null);
    }

    public String exceptionAsString(Throwable e)
    {
        if(userInfoProvider.isSystemDeveloper())
        {
            StringWriter sw = new StringWriter();
            if (e instanceof Be5Exception && e.getCause() != null)
            {
                e.getCause().printStackTrace(new PrintWriter(sw));
            } else
            {
                e.printStackTrace(new PrintWriter(sw));
            }
            return sw.toString();
        }else{
            return null;
        }
    }

    public ErrorModel getErrorModel(Be5Exception e)
    {
        return new ErrorModel(e.getHttpStatusCode(), e.getMessage(), Be5Exception.getMessage(e), exceptionAsString(e), null);
    }

    public ErrorModel getErrorModel(Be5Exception e, String additionalMessage, Map<String, String> links)
    {
        return new ErrorModel(e.getHttpStatusCode(), e.getMessage(), Be5Exception.getMessage(e) + additionalMessage,
                exceptionAsString(e), links);
    }

    public Object getDefaultMeta(Request request)
    {
        return Collections.singletonMap(TIMESTAMP_PARAM, request.get(TIMESTAMP_PARAM));
    }

}
