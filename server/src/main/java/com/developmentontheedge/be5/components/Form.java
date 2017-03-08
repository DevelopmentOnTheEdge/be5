package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.impl.DocumentResponse;

public class Form implements Component {
    
    public Form()
    {
        /* stateless */
    }

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        // TODO verify user roles
        
        DocumentResponse response = DocumentResponse.of(res);
        
        try
        {
            switch (req.getRequestUri())
            {
//            case "":
//                response.send(new FormGenerator(serviceProvider).generate(req));
//                return;
//            case "apply":
//                response.send(new OperationExecutor(serviceProvider).execute(req));
//                return;
            default:
                res.sendUnknownActionError();
                return;
            }
        }
        catch (Be5Exception ex)
        {
            if(ex.getCode().isInternal()) {
                serviceProvider.getLogger().error(ex);
            }
            res.sendError(ex);
        }
    }

}
