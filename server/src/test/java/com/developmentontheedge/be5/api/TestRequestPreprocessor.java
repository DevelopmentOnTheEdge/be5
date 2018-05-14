package com.developmentontheedge.be5.api;

import com.developmentontheedge.be5.api.support.RequestPreprocessorSupport;
import com.google.inject.Stage;


public class TestRequestPreprocessor extends RequestPreprocessorSupport
{
    private final Stage stage;

    public TestRequestPreprocessor(Stage stage)
    {
        this.stage = stage;
    }

    @Override
    public void preprocessUrl(Request req, Response res)
    {
        req.setAttribute("testRequestPreprocessor", stage.toString());
    }
}
