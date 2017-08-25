package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.components.impl.DocumentGenerator;
import com.developmentontheedge.be5.components.impl.DocumentResponse;
import com.developmentontheedge.be5.components.impl.MoreRowsGenerator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Document implements Component 
{
    private static final Logger log = Logger.getLogger(Document.class.getName());

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        DocumentResponse response = DocumentResponse.of(res);

        switch (req.getRequestUri())
        {
            case "":
                DocumentGenerator.generateAndSend(req, res, injector);
                return;
            case "moreRows":
                response.send(new MoreRowsGenerator(injector).generate(req));
                return;
            default:
                res.sendUnknownActionError();
        }
    }

}
