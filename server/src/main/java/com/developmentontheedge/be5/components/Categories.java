package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;

public class Categories implements Component
{

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch (req.getRequestUri())
        {
        case "forest":
            res.sendAsRawJson(injector.getCategoriesService().getCategoriesForest(req.getNonEmpty(RestApiConstants.ENTITY), req.getBoolean("hideEmpty", false)));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }

}
