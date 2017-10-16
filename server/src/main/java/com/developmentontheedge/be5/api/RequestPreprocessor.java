package com.developmentontheedge.be5.api;


public interface RequestPreprocessor
{
    void preprocessUrl(String componentId, Request req, Response res);
}
