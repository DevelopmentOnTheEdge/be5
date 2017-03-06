package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;

public class Categories implements Component
{
    
    public Categories()
    {
        // stateless
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        switch (req.getRequestUri())
        {
        case "forest":
            res.sendAsRawJson(serviceProvider.getCategoriesService().getCategoriesForest(req.getNonEmpty(RestApiConstants.ENTITY), req.get("hideEmpty", false)));
            return;
        default:
            res.sendUnknownActionError();
            return;
        }
    }

}
