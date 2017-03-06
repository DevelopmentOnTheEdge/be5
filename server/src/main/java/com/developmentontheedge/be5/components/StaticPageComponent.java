package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.components.impl.DocumentResponse;

public class StaticPageComponent implements Component {

	public StaticPageComponent()
	{
		// stateless
	}
	
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        String language = UserInfoManager.get(req, serviceProvider).getLanguage();
        String page = req.getRequestUri();
        DocumentResponse.of(res).sendStaticPage(serviceProvider.getProject().getStaticPageContent(language, page));
    }
	
}
