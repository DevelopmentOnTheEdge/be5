package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.ResponseHelper;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.exceptions.ErrorTitles;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.api.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class StaticPageController extends ControllerSupport
{
    private final ProjectProvider projectProvider;
    private final ResponseHelper responseHelper;

    @Inject
    public StaticPageController(ProjectProvider projectProvider, ResponseHelper responseHelper)
    {
        this.projectProvider = projectProvider;
        this.responseHelper = responseHelper;
    }

    @Override
    public void generate(Request req, Response res)
    {
        String language = UserInfoHolder.getLanguage();
        String page = req.getRequestUri();
        String staticPageContent = projectProvider.getProject().getStaticPageContent(language, page);

        if (staticPageContent == null)
        {
            String msg = ErrorTitles.formatTitle(Be5ErrorCode.NOT_FOUND, "static/" + page);
            log.fine(msg);

            //todo localize
            res.sendErrorAsJson(
                    new ErrorModel("404", msg,
                            Collections.singletonMap(SELF_LINK, "static/" + page)),
                    responseHelper.getDefaultMeta(req)
            );
        }
        else
        {
            res.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation("", staticPageContent),
                            Collections.singletonMap(SELF_LINK, "static/" + page)),
                    responseHelper.getDefaultMeta(req)
            );
        }
    }
	
}
