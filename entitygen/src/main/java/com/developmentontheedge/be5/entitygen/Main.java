package com.developmentontheedge.be5.entitygen;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main
{
    private static Injector injector = Be5.createInjector();

    public static void main(String[] args) throws Exception
    {
        new Main(args);
        System.exit(0);
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

        createEntities(generatedSourcesPath, cfg);
    }

    private void createEntities(String generatedSourcesPath, Configuration cfg) throws IOException
    {
        Template entityTpl = cfg.getTemplate("entity.ftl");
        String packageName = "com.developmentontheedge.be5.modules.core.generate.entities.".replace(".", "\\");

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
        }
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
