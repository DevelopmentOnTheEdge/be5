package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;

import static com.developmentontheedge.be5.components.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;


public class StaticPageComponent implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        String language = UserInfoHolder.getLanguage();
        String page = req.getRequestUri();
        String staticPageContent = injector.getProject().getStaticPageContent(language, page);

        if (staticPageContent == null)
        {
            res.sendError(Be5ErrorCode.NOT_FOUND.exception(page));
        }
        else
        {
            res.sendAsJson(
                    new ResourceData(STATIC_ACTION, new StaticPagePresentation("", staticPageContent)),
                    ImmutableMap.builder()
                            .put(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM))
                            .build(),
                    Collections.singletonMap(SELF_LINK, "static/" + page)
            );
        }
    }
	
}
