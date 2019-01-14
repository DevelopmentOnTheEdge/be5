package com.developmentontheedge.be5.operation.services;

import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.groovy.GroovyLoader;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.Operation;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroovyOperationLoader
{
    private final Meta meta;
    private final GroovyLoader groovyRegister;
    private final ProjectProvider projectProvider;

    private Map<String, GroovyOperation> groovyOperationsMap;

    @Inject
    public GroovyOperationLoader(ProjectProvider projectProvider, Meta meta, GroovyLoader groovyRegister)
    {
        this.groovyRegister = groovyRegister;
        this.meta = meta;
        this.projectProvider = projectProvider;
    }

    @Start(order = 30)
    public void start() throws Exception
    {
        projectProvider.addToReload(this::initOperationMap);
        initOperationMap();
    }

    void initOperationMap()
    {
        Map<String, com.developmentontheedge.be5.metadata.model.GroovyOperation> newOperationMap = new HashMap<>();
        List<Entity> entities = meta.getOrderedEntities("ru");
        for (Entity entity : entities)
        {
            List<String> operationNames = meta.getOperationNames(entity);
            for (String operationName : operationNames)
            {
                com.developmentontheedge.be5.metadata.model.Operation operation =
                        meta.getOperation(entity.getName(), operationName);
                if (operation.getClass() == GroovyOperation.class)
                {
                    GroovyOperation groovyOperation = (GroovyOperation) operation;
                    String fileName = groovyOperation.getFileName().replace("/", ".");
                    newOperationMap.put(fileName, groovyOperation);
                }
            }
        }

        groovyOperationsMap = newOperationMap;
    }

    public Operation getByFullName(String name)
    {
        return groovyOperationsMap.get(name.replace("/", "."));
    }

    public List<String> preloadSuperOperation(GroovyOperation operationMeta)
    {
        String simpleSuperClassName = getSimpleSuperClassName(operationMeta);
        String superOperationCanonicalName = getCanonicalSuperClassName(operationMeta);

        com.developmentontheedge.be5.metadata.model.Operation superOperation = groovyOperationsMap
                .get(superOperationCanonicalName);
        if (superOperation != null && superOperation.getClass() == GroovyOperation.class)
        {
            if (groovyRegister.getGroovyClasses().getIfPresent(superOperationCanonicalName) == null)
            {
                ArrayList<String> list = new ArrayList<>(preloadSuperOperation((GroovyOperation) superOperation));

                list.add(superOperationCanonicalName);
                groovyRegister.getClass(superOperationCanonicalName,
                        superOperation.getCode(), simpleSuperClassName + ".groovy");
                return list;
            }
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    public Class get(GroovyOperation groovyOperationMeta)
    {
        preloadSuperOperation(groovyOperationMeta);
        String fileName = groovyOperationMeta.getFileName();
        String canonicalName = fileName.replace("/", ".");
        String simpleName = fileName.substring(fileName.lastIndexOf("/") + 1,
                fileName.length() - ".groovy".length()).trim();

        return groovyRegister.getClass(canonicalName, groovyOperationMeta.getCode(), simpleName + ".groovy");
    }

    public String getSimpleSuperClassName(GroovyOperation groovyOperationMeta)
    {
        String fileName = groovyOperationMeta.getFileName();
        String className = fileName.substring(fileName.lastIndexOf("/") + 1,
                fileName.length() - ".groovy".length()).trim();
        String classBegin = "class " + className + " extends ";

        String code = groovyOperationMeta.getCode();
        int superClassBeginPos = code.indexOf(classBegin);
        if (superClassBeginPos == -1) return null;

        superClassBeginPos += classBegin.length();
        int superClassEndPos = Math.min(
                code.indexOf(" ", superClassBeginPos) != -1 ? code.indexOf(" ", superClassBeginPos) : 999999999,
                code.indexOf("\n", superClassBeginPos));


        return code.substring(superClassBeginPos, superClassEndPos).trim();
    }

    public String getCanonicalSuperClassName(GroovyOperation operationMeta)
    {
        String code = operationMeta.getCode();
        String superOperationName = getSimpleSuperClassName(operationMeta);

        String superOperationFullName = superOperationName + ".groovy";

        int lineBegin = code.indexOf("package ");
        if (lineBegin != -1)
        {
            int lineEnd = code.indexOf("\n", lineBegin);
            String line = code.substring(lineBegin, lineEnd);
            superOperationFullName = line.replace("package ", "")
                    .replace(";", "")
                    + "." + superOperationName + ".groovy";
        }

        lineBegin = code.indexOf("import ");
        while (lineBegin != -1)
        {
            int lineEnd = code.indexOf("\n", lineBegin);
            String line = code.substring(lineBegin, lineEnd);
            if (line.contains("." + superOperationName))
            {
                superOperationFullName = line.replace("import ", "")
                        .replace(";", "") + ".groovy";
            }
            lineBegin = code.indexOf("import ", lineEnd);
        }
        return superOperationFullName;
    }

}
