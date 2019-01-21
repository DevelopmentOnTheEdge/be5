package com.developmentontheedge.be5.web.impl;

import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class ControllerSupport extends HttpServlet implements Controller
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

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

    @Inject
    private Provider<Request> req;

    private void respond(HttpServletRequest request, HttpServletResponse response)
    {
        ResponseImpl res = new ResponseImpl(response);
        try
        {
            generate(req.get(), res);
        }
        catch (IllegalArgumentException e)
        {
            log.log(Level.SEVERE, "Error in controller", e);
            res.sendAsJson(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in controller", e);
            res.sendAsJson("Error in controller", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public abstract void generate(Request req, Response res);

    protected void sendHtmlError(Response response, Exception e)
    {
        sendHtmlError(response, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    protected void sendHtmlError(Response response, Exception e, int status)
    {
        response.setStatus(status);
        response.sendHtml(e.getMessage());
        log.log(Level.SEVERE, "", e);
    }
}
