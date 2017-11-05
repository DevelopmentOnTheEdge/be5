package com.developmentontheedge.be5.entitygen;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class GdslGenegator
{
    private Injector injector;
    private int entityCount = 0;

    public static void main(String[] args) throws Exception
    {
        new GdslGenegator(args);
    }

    public GdslGenegator(String[] args) throws IOException
    {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(Main.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");

        String generatedSourcesPath = args[0];
        String packageName = args[1];
        if(packageName == null){
            packageName = "";
        }else{
            if(!packageName.endsWith("."))packageName += ".";
        }

        String serviceClassName = args[2] + "DatabaseModel";

        File file = Paths.get(generatedSourcesPath + packageName.replace(".", "/") + serviceClassName + ".gdsl").toFile();

        if(file.exists() && !file.isDirectory())
        {
            System.out.println("Generate skipped, file exists: " + packageName + "." + serviceClassName);
        }
        else
        {
            System.out.println("File '"+file.toString()+"' not found, generate...");
            injector = Be5.createInjector(Stage.PRODUCTION, new YamlBinder(YamlBinder.Mode.serverOnly));

            createService(generatedSourcesPath, packageName, serviceClassName, cfg);

            System.out.println("------" + JULLogger.infoBlock(
                    "Generate successful: " + entityCount + " entities created.\n" +
                            packageName + serviceClassName));
        }
    }

    private void createService(String generatedSourcesPath, String packageName,
                               String serviceClassName, Configuration cfg) throws IOException
    {
        Template serviceTpl = cfg.getTemplate("gdsl/entities.ftl");

        Meta meta = injector.getMeta();
        List<Entity> entities = meta.getOrderedEntities("ru");

        Map<String, Object> input = new HashMap<>();
//        input.put("serviceClassName", serviceClassName);
//        input.put("packageName", packageName);

        List<String> entityNames = new ArrayList<>();
        entityCount = entities.size();
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            entityNames.add(entity.getName());
        }
        input.put("entityNames", entityNames);
        Utils.createFile(generatedSourcesPath, packageName, serviceClassName+ ".gdsl", serviceTpl, input);
    }


}
