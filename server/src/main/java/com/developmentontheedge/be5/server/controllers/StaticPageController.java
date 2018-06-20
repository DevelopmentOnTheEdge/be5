package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.servlet.support.ApiControllerSupport;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import java.util.Collections;
import java.util.logging.Level;

import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class StaticPageController extends ApiControllerSupport
{
    private final UserAwareMeta userAwareMeta;
    private final JsonApiResponseHelper responseHelper;

    @Inject
    public StaticPageController(JsonApiResponseHelper responseHelper, UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
        this.responseHelper = responseHelper;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String url = new HashUrl(STATIC_ACTION, requestSubUrl).toString();

        try{
            responseHelper.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation(
                            "",
                            userAwareMeta.getStaticPageContent(requestSubUrl)),
                            Collections.singletonMap(SELF_LINK, url)),
                    responseHelper.getDefaultMeta(req)
            );
        }
        catch(Be5Exception e)
        {
            log.log(Level.SEVERE, "Error in table: " + url + ", on requestSubUrl = '" + requestSubUrl + "'", e);
            responseHelper.sendErrorAsJson(e, req);
        }
    }
	
}
