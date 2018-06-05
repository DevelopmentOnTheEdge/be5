package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.services.CategoriesService;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.support.ApiControllerSupport;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;


public class CategoriesController extends ApiControllerSupport implements Controller
{
    private final CategoriesService categoriesService;

    @Inject
    public CategoriesController(CategoriesService categoriesService)
    {
        this.categoriesService = categoriesService;
    }

    @Override
    public void generate(Request req, Response res, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "forest":
                res.sendAsJson(categoriesService.getCategoriesForest(
                                req.getNonEmpty(RestApiConstants.ENTITY),
                                req.getBoolean("hideEmpty", false)
                ));
                return;
            default:
                res.sendErrorAsJson("Unknown action", HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
