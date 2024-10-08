package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.impl.ControllerSupport;
import com.developmentontheedge.be5.web.impl.ResponseImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class BaseControllerSupport extends ControllerSupport
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
    {
        respond(request, response);
    }

    @Inject
    private Provider<Request> req;

    protected void respond(HttpServletRequest request, HttpServletResponse response)
    {
        ServletUtils.addHeaders(request, response);
        Response res = new ResponseImpl(response);
        try
        {
            generate(req.get(), res);
        }
        catch (Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in controller", e);
            res.sendAsJson(e.getMessage(), Integer.parseInt(e.getHttpStatusCode()));
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

    String getApiSubUrl(Request req)
    {
        String requestUri = req.getRequestUri();

        String apiWithContext = req.getContextPath() + "/api/";

        int subIndex = requestUri.indexOf('/', apiWithContext.length());

        if (subIndex != -1)
        {
            return requestUri.substring(subIndex + 1, requestUri.length());
        }
        else
        {
            return "";
        }
    }
}
