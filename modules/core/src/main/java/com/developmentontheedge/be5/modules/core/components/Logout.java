package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;

public class Logout implements Component
{

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        injector.getLoginService().logout(req);
        res.sendSuccess();
    }

}
