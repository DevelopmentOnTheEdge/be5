package com.developmentontheedge.be5.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;

public class ScriptList implements Component
{
    private static final List<String> allowedCategories = Arrays.asList("components", "actions");

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        List<String> result = new ArrayList<>();
        String scriptCategory = req.get("category");

        if (!allowedCategories.contains(scriptCategory))
            throw Be5ErrorCode.PARAMETER_INVALID.exception("category", scriptCategory);

        try (Stream<Path> paths = Files.list(Paths.get(req.getRawRequest().getSession().getServletContext().getRealPath("/scripts/" + scriptCategory))))
        {
            paths.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".js"))
                    .map(n -> n.substring(0, n.length() - ".js".length()))
                    .forEach(result::add);
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e);
        }
        
        res.sendAsRawJson(result);
    }

}
