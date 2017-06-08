package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.impl.DocumentResponse;

public class StaticPageComponent implements Component {

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
            DocumentResponse.of(res).sendStaticPage(staticPageContent);
        }
    }
	
}
