package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.api.services.CategoriesService;

import com.google.inject.Inject;


public class Categories extends ControllerSupport implements Controller
{
    private final CategoriesService categoriesService;

    @Inject
    public Categories(CategoriesService categoriesService)
    {
        this.categoriesService = categoriesService;
    }

    @Override
    public void generate(Request req, Response res)
    {
        switch (req.getRequestUri())
        {
            case "forest":
                res.sendAsRawJson(categoriesService.getCategoriesForest(
                                req.getNonEmpty(RestApiConstants.ENTITY),
                                req.getBoolean("hideEmpty", false)
                ));
                return;
            default:
                res.sendUnknownActionError();
        }
    }

}
