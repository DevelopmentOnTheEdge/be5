package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.google.common.collect.ImmutableMap;

public class Logout implements Component
{

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        serviceProvider.getLoginService().logout(req);
        res.sendSuccess();
    }

}
