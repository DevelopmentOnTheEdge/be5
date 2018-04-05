package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.modules.core.services.CategoriesService;


public class Categories implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        switch (req.getRequestUri())
        {
            case "forest":
                res.sendAsRawJson(injector.get(CategoriesService.class).getCategoriesForest(
                                req.getNonEmpty(RestApiConstants.ENTITY),
                                req.getBoolean("hideEmpty", false)
                ));
                return;
            default:
                res.sendUnknownActionError();
        }
    }

}
