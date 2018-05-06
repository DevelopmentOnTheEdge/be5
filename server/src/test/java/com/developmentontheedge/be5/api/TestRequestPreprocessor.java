package com.developmentontheedge.be5.api;


import com.google.inject.Stage;

public class TestRequestPreprocessor implements RequestPreprocessor
{
    private final Stage stage;

    public TestRequestPreprocessor(Stage stage)
    {
        this.stage = stage;
    }

    @Override
    public void preprocessUrl(String componentId, Request req, Response res)
    {
        req.setAttribute("testRequestPreprocessor", stage.toString());
    }
}
