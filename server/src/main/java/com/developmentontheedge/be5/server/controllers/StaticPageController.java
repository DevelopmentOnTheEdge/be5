package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.support.ApiControllerSupport;

import javax.inject.Inject;
import java.util.Collections;
import java.util.logging.Level;

import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class StaticPageController extends ApiControllerSupport
{
    private final ProjectProvider projectProvider;
    private final JsonApiResponseHelper responseHelper;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public StaticPageController(ProjectProvider projectProvider, JsonApiResponseHelper responseHelper, UserInfoProvider userInfoProvider)
    {
        this.projectProvider = projectProvider;
        this.responseHelper = responseHelper;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String language = userInfoProvider.get().getLanguage();
        String staticPageContent = projectProvider.get().getStaticPageContent(language, requestSubUrl);

        if (staticPageContent == null)
        {
            Be5Exception be5Exception = Be5Exception.notFound("static/" + requestSubUrl);

            log.log(Level.INFO, "", be5Exception);

            //todo localize
            responseHelper.sendErrorAsJson(
                    new ErrorModel("404", be5Exception.getMessage(),
                            Collections.singletonMap(SELF_LINK, "static/" + requestSubUrl)),
                    responseHelper.getDefaultMeta(req)
            );
        }
        else
        {
            responseHelper.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation("", staticPageContent),
                            Collections.singletonMap(SELF_LINK, "static/" + requestSubUrl)),
                    responseHelper.getDefaultMeta(req)
            );
        }
    }
	
}
