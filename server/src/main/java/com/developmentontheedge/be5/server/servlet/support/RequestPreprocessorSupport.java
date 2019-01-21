package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.RequestPreprocessor;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public abstract class RequestPreprocessorSupport implements RequestPreprocessor, Filter
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
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ServletUtils.addHeaders(request, response);
        Response res = new ResponseImpl(response);
        preprocessUrl(req.get(), res);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }

}
