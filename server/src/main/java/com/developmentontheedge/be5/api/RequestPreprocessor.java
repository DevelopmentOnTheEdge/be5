package com.developmentontheedge.be5.api;


public interface RequestPreprocessor
{
    String preprocessUrl(Request req, Response res, String componentId);
}
