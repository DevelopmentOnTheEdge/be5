package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.modules.core.services.CategoriesService;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategoriesController extends JsonApiController
{
    private final CategoriesService categoriesService;

    @Inject
    public CategoriesController(CategoriesService categoriesService)
    {
        this.categoriesService = categoriesService;
    }

    @Override
    protected Object generate(Request req, String action)
    {
        switch (action)
        {
            case "forest":
                return categoriesService.getCategoriesForest(
                        req.getNonEmpty(RestApiConstants.ENTITY_NAME_PARAM),
                        req.getBoolean("hideEmpty", false)
                );
            default:
                return null;
        }
    }

}
