package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.components.impl.DocumentResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Form implements Component
{
    private static final Logger log = Logger.getLogger(Document.class.getName());

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        OperationService operationService = injector.get(OperationService.class);
        
        DocumentResponse response = DocumentResponse.of(res);
        
        try
        {
            switch (req.getRequestUri())
            {
            case "":
                response.send(operationService.generate(req));
                return;
            case "apply":
                response.send(operationService.execute(req));
                return;
            default:
                res.sendUnknownActionError();
                return;
            }
        }
        catch (Be5Exception ex)
        {
            if(ex.getCode().isInternal()) {
                log.log(Level.SEVERE, ex.getMessage(), ex);
            }
            res.sendError(ex);
        }
    }

}
