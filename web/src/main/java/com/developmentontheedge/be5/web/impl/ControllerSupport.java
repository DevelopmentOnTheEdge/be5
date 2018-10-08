package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class ControllerSupport extends HttpServlet implements Controller
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        respond(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        respond(request, response);
    }

    private void respond(HttpServletRequest request, HttpServletResponse response)
    {
        Request req = new RequestImpl(request);
        ResponseImpl res = new ResponseImpl(response);
        generate(req, res);
    }

    public abstract void generate(Request req, Response res);
}
