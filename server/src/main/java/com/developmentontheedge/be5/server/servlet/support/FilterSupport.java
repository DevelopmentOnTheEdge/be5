package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.impl.ResponseImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class FilterSupport implements Filter
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    @Inject
    private Provider<Request> req;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Response res = new ResponseImpl(response);

        try
        {
            filter(req.get(), res, filterChain);
        }
        catch (Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in filter", e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.sendHtml(e.getMessage() != null ? e.getMessage() : "");
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in filter", e);
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.sendHtml(e.getMessage() != null ? e.getMessage() : "");
        }
    }

    @Override
    public void destroy()
    {

    }

    public abstract void filter(Request req, Response res, FilterChain chain) throws IOException, ServletException;
}
