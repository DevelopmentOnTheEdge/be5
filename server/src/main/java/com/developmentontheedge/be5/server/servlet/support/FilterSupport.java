package com.developmentontheedge.be5.server.servlet.support;

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


public abstract class FilterSupport implements Filter
{
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
        filter(req.get(), res, filterChain);
    }

    @Override
    public void destroy()
    {

    }

    public abstract void filter(Request req, Response res, FilterChain chain) throws IOException, ServletException;
}
