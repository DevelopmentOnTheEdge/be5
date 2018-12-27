package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.servlet.http.HttpServletResponse;


public abstract class JsonApiController extends BaseControllerSupport
{
    @Override
    public void generate(Request req, Response res)
    {
        Object object = generate(req);
        if (object != null)
        {
            res.sendAsJson(object);
        }
        else
        {
            res.sendAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public Object generate(Request req)
    {
        return generate(req, getApiSubUrl(req));
    }

    protected abstract Object generate(Request req, String action);
}
