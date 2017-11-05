package com.developmentontheedge.be5.entitygen;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.google.inject.internal.util.Strings;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main
{
    private Injector injector;
    private int entityCount = 0;

    public static void main(String[] args) throws Exception
    {
        new Main(args);
    }

    public Main(String[] args) throws IOException
    {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(Main.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");

        String generatedSourcesPath = args[0];
        String packageName = args[1];
        if(!packageName.endsWith("."))packageName += ".";
        packageName += args[2].toLowerCase()+".generate";

        String serviceClassName = args[2] + "EntityModels";

        File file = Paths.get(generatedSourcesPath + packageName.replace(".", "/") + "/" + serviceClassName + ".java").toFile();
        if(file.exists() && !file.isDirectory())
        {
            System.out.println("Generate skipped, file exists: " + packageName + "." + serviceClassName);
        }
        else
        {
            System.out.println("File '"+file.toString()+"' not found, generate...");
            injector = Be5.createInjector(new YamlBinder(YamlBinder.Mode.serverOnly));

            createClass(generatedSourcesPath,"","package-info", cfg.getTemplate("root.ftl"), Collections.emptyMap());

            createEntities(generatedSourcesPath, packageName + ".entities", cfg);
            createService(generatedSourcesPath, packageName, serviceClassName, cfg);

            System.out.println("------" + JULLogger.infoBlock(
                    "Generate successful: " + entityCount + " entities created.\n" +
                            "Add service to context.yaml: " +
                            packageName + "." + serviceClassName));
        }
    }

//    private String artifactIdToPackage(String path)
//    {
//        StringBuilder s = new StringBuilder();
//        boolean toUpperCase = false;
//        path = path.toLowerCase();
//        for (int i = 0; i < path.length(); i++)
//        {
//            if(path.charAt(i) == '-')
//            {
//                toUpperCase = true;
//                continue;
//            }
//            if(toUpperCase){
//                s.append(Character.toUpperCase(path.charAt(i)));
//                toUpperCase = false;
//            }else{
//                s.append(path.charAt(i));
//            }
//        }
//
//        return s.toString();
//    }

    private void createEntities(String generatedSourcesPath, String packageName, Configuration cfg) throws IOException
    {
        Template entityTpl = cfg.getTemplate("entity.ftl");

        Meta meta = injector.getMeta();
        List<Entity> entities = meta.getOrderedEntities("ru");
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            String entityClassName = Strings.capitalize(entity.getName());
            Map<String, Object> input = new HashMap<>();
            input.put("entityClassName", entityClassName);
            input.put("packageName", packageName);

            Map<String, ColumnDef> columns = meta.getColumns(entity);

            List<ColumnsInfo> columnsInfos = new ArrayList<>();
            for(ColumnDef columnDef : columns.values())
            {
                columnsInfos.add(new ColumnsInfo(columnDef.getName(), meta.getColumnType(columnDef).getSimpleName()));
            }
            input.put("columns", columnsInfos);
            createClass(generatedSourcesPath, packageName, entityClassName, entityTpl, input);
            entityCount++;
        }
    }

    private void createService(String generatedSourcesPath, String packageName,
                               String serviceClassName, Configuration cfg) throws IOException
    {
        Template serviceTpl = cfg.getTemplate("service.ftl");

        Meta meta = injector.getMeta();
        List<Entity> entities = meta.getOrderedEntities("ru");

        Map<String, Object> input = new HashMap<>();
        input.put("serviceClassName", serviceClassName);
        input.put("packageName", packageName);

        List<String> entityNames = new ArrayList<>();
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            entityNames.add(entity.getName());
        }
        input.put("entityNames", entityNames);
        createClass(generatedSourcesPath, packageName, serviceClassName, serviceTpl, input);
    }

    private void createClass(String generatedSourcesPath, String packageName, String className,
                                    Template template, Map<String, Object> input) throws IOException
    {
        Paths.get(generatedSourcesPath + packageName.replace(".", "/")).toFile().mkdirs();
        Writer fileWriter = new FileWriter(new File(generatedSourcesPath + packageName.replace(".", "/")+"/"+className + ".java"));

        try
        {
            template.process(input, fileWriter);
        }
        catch (TemplateException e)
        {
            e.printStackTrace();
        }
        finally
        {
            fileWriter.close();
        }
    }

    public class ColumnsInfo{
        public String name;
        public String type;

        public ColumnsInfo(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }
    }
}
