package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.impl.DocumentResponse;

public class StaticPageComponent implements Component {

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        String language = UserInfoHolder.getLanguage();
        String page = req.getRequestUri();
        String staticPageContent = serviceProvider.getProject().getStaticPageContent(language, page);

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
