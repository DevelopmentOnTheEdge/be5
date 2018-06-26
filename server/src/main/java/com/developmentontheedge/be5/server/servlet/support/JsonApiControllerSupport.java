package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.server.util.annotations.Experimental;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletResponse;

@Experimental
public abstract class JsonApiControllerSupport extends ApiControllerSupport
{
    @Override
    protected final void generate(Request req, Response res, String subUrl)
    {
        Object object = generate(req, subUrl);
        if(object != null)
        {
            res.sendAsJson(generate(req, subUrl));
        }
        else
        {
            res.sendErrorAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected abstract Object generate(Request req, String subUrl);
}
