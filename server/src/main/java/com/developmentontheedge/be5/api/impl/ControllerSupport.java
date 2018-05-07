package com.developmentontheedge.be5.api.impl;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class ControllerSupport extends HttpServlet implements Controller
{
    private final static Pattern uriPattern = Pattern.compile("(/.*)?/api/(.*)");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        respond(request, response, request.getMethod(), request.getRequestURI(), request.getParameterMap());
    }

    /**
     * The general routing method. Tries to determine and find a component using a given request URI.
     * Generation of response is delegated to a found component.
     */
    private void respond(HttpServletRequest request, HttpServletResponse response, String method, String requestUri, Map<String, String[]> parameters)
    {
        Matcher matcher = uriPattern.matcher(requestUri);
        if (!matcher.matches())
        {
            throw new RuntimeException("must be bind to /api/");
        }

        String[] uriParts = requestUri.split("/");
        int ind = 1;

        while (!"api".equals(uriParts[ind]) && ind + 1 < uriParts.length)
        {
            ind++;
        }

        String subRequestUri = Joiner.on('/').join(Iterables.skip(Arrays.asList(uriParts), ind + 2));

        Request req = new RequestImpl(request, subRequestUri);
        UserInfoHolder.setRequest(req);

        generate(req, getResponse(request, response));
    }

    private Response getResponse(HttpServletRequest request, HttpServletResponse response)
    {
        String origin = request.getHeader("Origin");// TODO test origin

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        return new ResponseImpl(response);
    }

}
