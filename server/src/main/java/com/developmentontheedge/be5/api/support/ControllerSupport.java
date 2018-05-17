package com.developmentontheedge.be5.api.support;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.RequestImpl;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Request req = new RequestImpl(request, ServletUtils.getApiSubUrl(request.getRequestURI()));

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
}
