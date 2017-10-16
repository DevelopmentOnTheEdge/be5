package com.developmentontheedge.be5.api;


public class TestRequestPreprocessor implements RequestPreprocessor
{
    @Override
    public String preprocessUrl(String componentId, Request req, Response res)
    {
        req.setAttribute("testRequestPreprocessor", 1);
        return null;
    }
}
