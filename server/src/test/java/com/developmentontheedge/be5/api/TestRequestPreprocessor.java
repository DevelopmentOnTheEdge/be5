package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.Inject;


public class TestRequestPreprocessor implements RequestPreprocessor
{
    @Inject ProjectProvider projectProvider;

    @Override
    public void preprocessUrl(String componentId, Request req, Response res)
    {
        req.setAttribute("testRequestPreprocessor", projectProvider.getProject().getName());
    }
}
