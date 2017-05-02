package com.developmentontheedge.be5.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.google.common.collect.ComparisonChain;

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

        Collections.sort(result);
        res.sendAsRawJson(result);
    }

    private void load(Request req, List<ActionPaths> result, String scriptCategory, String module)
    {
        boolean isModule = !"".equals(module);
        String sPath = scriptCategory + (isModule ? "/" + module : "") + "/" + actionsCategory;
        Path path = Paths.get(req.getServletContextRealPath(sPath));

        if(!Files.exists(path))return;

        try (Stream<Path> paths = Files.list(path))
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

    static class ActionPaths implements Comparable<ActionPaths>{
        String name;
        String path;

        ActionPaths(String name, String path)
        {
            this.name = name;
            this.path = path;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActionPaths that = (ActionPaths) o;

            return (name != null ? name.equals(that.name) : that.name == null) &&
                   (path != null ? path.equals(that.path) : that.path == null);
        }

        @Override
        public String toString()
        {
            return "ActionPaths{" +
                    "name='" + name + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }

        @Override
        public int compareTo(ActionPaths o)
        {
            return ComparisonChain.start()
                    .compare(name, o.name)
                    .compare(path, o.path)
                    .result();
        }
    }

}
