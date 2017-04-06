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
    private static final String be5Category = "be5/scripts/be5/actions";

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        List<ActionPaths> result = new ArrayList<>();
        String scriptCategory = req.get("category");

        //TODO find and load also modules action
        load(req, result, be5Category);
        load(req, result, scriptCategory);

        res.sendAsRawJson(result);
    }

    private void load(Request req, List<ActionPaths> result, String scriptCategory)
    {
        try (Stream<Path> paths = Files.list(Paths.get(req.getRawRequest().getSession().getServletContext().getRealPath(scriptCategory))))
        {
            paths.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".js"))
                    .map(n -> new ActionPaths(n.substring(0, n.length() - ".js".length()), scriptCategory + "/" + n))
                    .forEach(result::add);
        }
        catch (IOException e)
        {
            throw Be5Exception.internal(e);
        }
    }

    class ActionPaths{
        String name;
        String path;

        public ActionPaths(String name, String path)
        {
            this.name = name;
            this.path = path;
        }
    }

}
