package com.developmentontheedge.be5.env.impl.testComponents;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;


public class TestComponentWithInjectAnnotation implements Component
{
    @Inject private SqlService db;

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        db.update("select 1");
    }
}
