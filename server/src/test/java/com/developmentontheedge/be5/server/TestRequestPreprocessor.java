package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.server.servlet.support.RequestPreprocessorSupport;
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
        req.getSession().set("testRequestPreprocessor", stage.toString());
    }
}
