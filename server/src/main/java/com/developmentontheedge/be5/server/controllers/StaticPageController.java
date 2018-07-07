package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class StaticPageController extends JsonApiModelController
{
    private final UserAwareMeta userAwareMeta;
    private final ErrorModelHelper responseHelper;

    @Inject
    public StaticPageController(ErrorModelHelper responseHelper, UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
        this.responseHelper = responseHelper;
    }

    @Override
    public JsonApiModel generate(Request req, String requestSubUrl)
    {
        String url = new HashUrl(STATIC_ACTION, requestSubUrl).toString();

        try{
            return data(new ResourceData(STATIC_ACTION, new StaticPagePresentation(
                    "",
                    userAwareMeta.getStaticPageContent(requestSubUrl)),
                    Collections.singletonMap(SELF_LINK, url)));
        }
        catch(Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in static page: " + url + ", on requestSubUrl = '" + requestSubUrl + "'", e);
            return error(responseHelper.getErrorModel(e, Collections.singletonMap(SELF_LINK, url)));
        }
    }

}
