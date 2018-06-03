package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.model.jsonapi.ErrorModel;

import javax.inject.Inject;
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

    @Inject
    public JsonApiResponseHelper(UserInfoProvider userInfoProvider)
    {
        this.userInfoProvider = userInfoProvider;
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
        return new ErrorModel(e.getHttpStatusCode(), e.getTitle(), Be5Exception.getMessage(e), exceptionAsString(e), null);
    }

    public ErrorModel getErrorModel(Be5Exception e, String additionalMessage, Map<String, String> links)
    {
        return new ErrorModel(e.getHttpStatusCode(), e.getTitle(), Be5Exception.getMessage(e) + additionalMessage,
                exceptionAsString(e), links);
    }

    public Object getDefaultMeta(Request request)
    {
        return Collections.singletonMap(TIMESTAMP_PARAM, request.get(TIMESTAMP_PARAM));
    }

}
