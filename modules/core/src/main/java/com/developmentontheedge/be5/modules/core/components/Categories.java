package com.developmentontheedge.be5.modules.core.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.services.CategoriesService;


public class Categories implements Component
{
    private final CategoriesService categoriesService;

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
