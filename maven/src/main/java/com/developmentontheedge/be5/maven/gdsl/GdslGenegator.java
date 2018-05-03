package com.developmentontheedge.be5.maven.gdsl;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;


public class GdslGenegator
{
    private static final Logger log = Logger.getLogger(GdslGenegator.class.getName());

    private int entityCount = 0;

    public static void main(String[] args) throws Exception
    {
        new GdslGenegator(args);
    }

    public GdslGenegator(String[] args) throws IOException
    {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(GdslGenegator.class, "/templates");
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
            log.info("Generate skipped, file exists: " + packageName + "." + serviceClassName);
            return;
        }

        log.info("File '"+file.toString()+"' not found, generate...");

        try
        {
            createService(generatedSourcesPath, packageName, serviceClassName, cfg);
        }
        catch (ProjectLoadException e)
        {
            e.printStackTrace();
        }

        log.info("Generate successful: " + entityCount + " entities added.\n" + packageName + serviceClassName);
    }

    private void createService(String generatedSourcesPath, String packageName,
                               String serviceClassName, Configuration cfg) throws IOException, ProjectLoadException
    {
        Template serviceTpl = cfg.getTemplate("gdsl/entities.ftl");

        Project project = ModuleLoader2.findAndLoadProjectWithModules();
        List<Entity> entities = project.getAllEntities();

        Map<String, Object> input = new HashMap<>();
//        input.put("serviceClassName", serviceClassName);
//        input.put("packageName", packageName);

        List<String> entityNames = new ArrayList<>();
        entityCount = entities.size();
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            if(entity.getName().equals("properties"))continue;//groovy have getProperties()
            entityNames.add(entity.getName());
        }
        input.put("entityNames", entityNames);
        Utils.createFile(generatedSourcesPath, packageName, serviceClassName+ ".gdsl", serviceTpl, input);
    }


}
