package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.support.ApiControllerSupport;
import com.developmentontheedge.be5.base.exceptions.ErrorTitles;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.web.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.web.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class StaticPageController extends ApiControllerSupport
{
    private final ProjectProvider projectProvider;
    private final JsonApiResponseHelper responseHelper;

    @Inject
    public StaticPageController(ProjectProvider projectProvider, JsonApiResponseHelper responseHelper)
    {
        this.projectProvider = projectProvider;
        this.responseHelper = responseHelper;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        String language = UserInfoHolder.getLanguage();
        String staticPageContent = projectProvider.getProject().getStaticPageContent(language, requestSubUrl);

        if (staticPageContent == null)
        {
            String msg = ErrorTitles.formatTitle(Be5ErrorCode.NOT_FOUND, "static/" + requestSubUrl);
            log.fine(msg);

            //todo localize
            res.sendErrorAsJson(
                    new ErrorModel("404", msg,
                            Collections.singletonMap(SELF_LINK, "static/" + requestSubUrl)),
                    responseHelper.getDefaultMeta(req)
            );
        }
        else
        {
            res.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation("", staticPageContent),
                            Collections.singletonMap(SELF_LINK, "static/" + requestSubUrl)),
                    responseHelper.getDefaultMeta(req)
            );
        }
    }
	
}
