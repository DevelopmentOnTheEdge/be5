package com.developmentontheedge.be5.api.support;

import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;


public class ServletUtils
{
    static Response getResponse(HttpServletRequest request, HttpServletResponse response)
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

    static String getApiSubUrl(String requestUri)
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
