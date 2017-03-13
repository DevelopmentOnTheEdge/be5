package com.developmentontheedge.be5.components;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.experimental.Be5Servlet;
import com.developmentontheedge.be5.env.LegacyServletConfig;

public class LegacyServlet implements Component
{
	@Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        final String uri = req.getRequestUri();
        HttpServlet servlet = serviceProvider.get(LegacyServletProvider.class).get(uri);

        if (servlet instanceof Be5Servlet)
        {
            ((Be5Servlet) servlet).initialize(serviceProvider);
        }

        if (servlet == null)
        {
            throw Be5ErrorCode.PARAMETER_INVALID.exception("servlet", uri);
        }
        try
        {
            servlet.init(new LegacyServletConfig(uri));
            servlet.service(req.getRawRequest(), res.getRawResponse());
        }
        catch (ServletException | IOException e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
