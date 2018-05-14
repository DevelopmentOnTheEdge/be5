package com.developmentontheedge.be5.api;


public interface RequestPreprocessor
{
    void preprocessUrl(Request req, Response res);
}
