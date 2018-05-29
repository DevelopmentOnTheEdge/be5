package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.api.services.CategoriesService;

import javax.inject.Inject;


public class CategoriesController extends ControllerSupport implements Controller
{
    private final CategoriesService categoriesService;

    @Inject
    public CategoriesController(CategoriesService categoriesService)
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
