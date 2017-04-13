package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.impl.DocumentGenerator;
import com.developmentontheedge.be5.components.impl.DocumentResponse;
import com.developmentontheedge.be5.components.impl.MoreRowsGenerator;
import com.developmentontheedge.be5.components.impl.TableCounter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Document implements Component 
{
    private static final Logger log = Logger.getLogger(Document.class.getName());

    public Document()
    {
        /* stateless */
    }
    
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        // TODO verify user roles
        
        try
        {
            DocumentResponse response = DocumentResponse.of(res);
            
            switch (req.getRequestUri())
            {
            case "":
                DocumentGenerator.generateAndSend(req, res, serviceProvider);
                return;
            case "count":
                TableCounter.countAndSend(req, res, serviceProvider);
                return;
            case "moreRows":
                response.send(new MoreRowsGenerator(serviceProvider).generate(req));
                return;
            default:
                res.sendUnknownActionError();
                return;
            }
        }
        catch (Be5Exception ex)
        {
            if(ex.getCode().isInternal()) 
            {
                log.log(Level.SEVERE, ex.getMessage(), ex);
            }
            res.sendError(ex);
        }
    }

}
