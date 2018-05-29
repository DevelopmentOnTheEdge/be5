package com.developmentontheedge.be5.server.support;

import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class ControllerSupport extends HttpServlet implements Controller
{
    protected static final Logger log = Logger.getLogger(ControllerSupport.class.getName());

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
        Request req = new RequestImpl(request, getApiSubUrl(request.getRequestURI()));

        UserInfoHolder.setRequest(req);

        try
        {
            generate(req, ServletUtils.getResponse(request, response));
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in controller", e);
        }
    }

    private String getApiSubUrl(String requestUri)
    {
        String[] uriParts = requestUri.split("/");
        int ind = 1;

        while (!"api".equals(uriParts[ind]) && ind + 1 < uriParts.length)
        {
            ind++;
        }

        return Joiner.on('/').join(Iterables.skip(Arrays.asList(uriParts), ind + 2));
    }
}
