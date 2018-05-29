package com.developmentontheedge.be5.api.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.RequestPreprocessor;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.web.impl.RequestImpl;

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
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        Request req = new RequestImpl(request, request.getRequestURI());

        UserInfoHolder.setRequest(req);

        preprocessUrl(req, ServletUtils.getResponse(request, response));

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }

}
