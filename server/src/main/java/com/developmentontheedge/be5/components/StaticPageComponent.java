package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.impl.ControllerSupport;
import com.developmentontheedge.be5.exceptions.ErrorTitles;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;

import com.google.inject.Inject;
import java.util.Collections;

import static com.developmentontheedge.be5.api.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class StaticPageComponent extends ControllerSupport
{
    private final ProjectProvider projectProvider;

    @Inject
    public StaticPageComponent(ProjectProvider projectProvider)
    {
        this.projectProvider = projectProvider;
    }

    @Override
    public void generate(Request req, Response res)
    {
        String language = UserInfoHolder.getLanguage();
        String page = req.getRequestUri();
        String staticPageContent = projectProvider.getProject().getStaticPageContent(language, page);

        if (staticPageContent == null)
        {
            //todo localize
            res.sendErrorAsJson(
                    new ErrorModel("500", ErrorTitles.formatTitle(Be5ErrorCode.NOT_FOUND, page),
                            Collections.singletonMap(SELF_LINK, "static/" + page)),
                    req.getDefaultMeta()
            );
        }
        else
        {
            res.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation("", staticPageContent),
                            Collections.singletonMap(SELF_LINK, "static/" + page)),
                    req.getDefaultMeta()
            );
        }
    }
	
}
