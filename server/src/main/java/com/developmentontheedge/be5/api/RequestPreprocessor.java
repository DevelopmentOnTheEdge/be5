package com.developmentontheedge.be5.api;


public interface RequestPreprocessor
{
    String preprocessUrl(String componentId, Request req, Response res);
}
