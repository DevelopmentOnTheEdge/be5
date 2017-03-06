package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.google.common.collect.ImmutableMap;

public class Logout implements Component
{

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        UserInfoManager.get(req, serviceProvider).logout();
        res.sendAsRawJson(ImmutableMap.of());
    }

}
