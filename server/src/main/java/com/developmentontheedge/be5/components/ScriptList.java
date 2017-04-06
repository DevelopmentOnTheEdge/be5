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
    private static final String actionsCategory = "actions";

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        List<ActionPaths> result = new ArrayList<>();

        String scriptCategory = req.get("category");
        if(scriptCategory != null)load(req, result, scriptCategory, "");

        //TODO find and load also modules action
        load(req, result, "be5/scripts", "be5");
        //result.add(new ActionPaths("static", "be5:be5/actions/static"));

        res.sendAsRawJson(result);
    }

    private void load(Request req, List<ActionPaths> result, String scriptCategory, String module)
    {
        boolean isModule = !"".equals(module);
        try (Stream<Path> paths = Files.list(Paths.get(req.getRawRequest().getSession().getServletContext()
                .getRealPath(scriptCategory +
                        (isModule ? "/" + module : "") +
                        "/" + actionsCategory
                ))))
        {
            paths.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".js"))
                    .map(n -> {
                        String name = n.substring(0, n.length() - ".js".length());
                        if(isModule){
                            return new ActionPaths(name, module + ":" + module + "/actions/" + name);
                        }else{
                            return new ActionPaths(name, "actions/" + name);
                        }
                    })
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
