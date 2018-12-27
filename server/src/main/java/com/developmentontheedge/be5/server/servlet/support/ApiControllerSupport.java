package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;


public abstract class ApiControllerSupport extends BaseControllerSupport
{
    @Override
    public final void generate(Request req, Response res)
    {
        generate(req, res, getApiSubUrl(req));
    }

    protected abstract void generate(Request req, Response res, String subUrl);
}
