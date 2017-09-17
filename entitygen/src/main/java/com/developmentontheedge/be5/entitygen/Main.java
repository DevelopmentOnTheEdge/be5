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

        String generatedSourcesPath;
        if(args.length >0){
            generatedSourcesPath = args[0];
        }else{
            generatedSourcesPath = "C:\\java\\dote\\github\\be5\\entitygen\\target\\generated-sources\\java\\";
        }
        String packageName = "com.developmentontheedge.be5.modules.core.generate.".replace(".", "\\");


        if(!Paths.get(generatedSourcesPath + packageName).toFile().isDirectory())
        {
            injector = Be5.createInjector(new YamlBinder(YamlBinder.Mode.serverOnly));
            String projectName = Strings.capitalize(injector.getProject().getName()) + "EntityModels";

            createClass(generatedSourcesPath,"","package-info.java",cfg.getTemplate("root.ftl"), Collections.emptyMap());
            createEntities(generatedSourcesPath + packageName, cfg);
            createService(generatedSourcesPath + packageName, cfg);
            System.out.println("------" + JULLogger.infoBlock(
                    "Generate successful: " + entityCount + " entities created.\n" +
                            "Add service to context.yaml: " +
                            "com.developmentontheedge.be5.modules.core.generate." + projectName));
            System.exit(0);//todo delete, fix ProjectProviderImpl - disable run watcher without dev.yaml
        }
        else
        {
            System.out.println("Skip generate - com.developmentontheedge.be5.modules.core.generate.entities exists");
        }
    }

    private void createEntities(String generatedSourcesPath, Configuration cfg) throws IOException
    {
        Template entityTpl = cfg.getTemplate("entity.ftl");
        String packageName = "entities.".replace(".", "\\");

        Meta meta = injector.getMeta();
        List<Entity> entities = meta.getOrderedEntities("ru");
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            String entityClassName = Strings.capitalize(entity.getName());
            Map<String, Object> input = new HashMap<>();
            input.put("entityClassName", entityClassName);

            Map<String, ColumnDef> columns = meta.getColumns(entity);

            List<ColumnsInfo> columnsInfos = new ArrayList<>();
            for(ColumnDef columnDef : columns.values())
            {
                columnsInfos.add(new ColumnsInfo(columnDef.getName(), meta.getColumnType(columnDef).getSimpleName()));
            }
            input.put("columns", columnsInfos);
            createClass(generatedSourcesPath, packageName, entityClassName + ".java", entityTpl, input);
            entityCount++;
        }
    }

    private void createService(String generatedSourcesPath, Configuration cfg) throws IOException
    {
        Template serviceTpl = cfg.getTemplate("service.ftl");

        Meta meta = injector.getMeta();
        List<Entity> entities = meta.getOrderedEntities("ru");

        Map<String, Object> input = new HashMap<>();
        String projectName = Strings.capitalize(injector.getProject().getName()) + "EntityModels";
        input.put("projectName", projectName);

        List<String> entityNames = new ArrayList<>();
        for(Entity entity : entities)
        {
            if(entity.getName().startsWith("_"))continue;
            entityNames.add(entity.getName());
        }
        input.put("entityNames", entityNames);
        createClass(generatedSourcesPath, "", projectName + ".java", serviceTpl, input);
    }

    private void createClass(String generatedSourcesPath, String packageName, String className,
                                    Template template, Map<String, Object> input) throws IOException
    {
        Paths.get(generatedSourcesPath+packageName).toFile().mkdirs();
        Writer fileWriter = new FileWriter(new File(generatedSourcesPath+packageName+className));

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
