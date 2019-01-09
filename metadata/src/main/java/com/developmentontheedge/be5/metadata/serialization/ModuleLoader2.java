package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.Features.BE_SQL_QUERIES;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;


public class ModuleLoader2
{
    private static final Logger log = Logger.getLogger(ModuleLoader2.class.getName());

    private static List<URL> urls;
    private static Map<String, Project> modulesMap;
    private static Map<String, Path> pathsToProjectsToHotReload = new HashMap<>();
    private static List<String> devRoles = new ArrayList<>();

    public static Project loadProjectWithModules(Path projectPath, ProcessController logger)
            throws ProjectLoadException, MalformedURLException
    {
        loadAllProjects(false, singletonList(projectPath.resolve("project.yaml").toUri().toURL()), logger);

        return findProjectAndMergeModules(logger);
    }

    public static Project findAndLoadProjectWithModules(boolean dirty) throws ProjectLoadException
    {
        JULLogger julLogger = new JULLogger(log);
        loadAllProjects(dirty, julLogger);

        return findProjectAndMergeModules(julLogger);
    }

    private static Project findProjectAndMergeModules(ProcessController logger) throws ProjectLoadException
    {
        Project project = null;

        if (modulesMap.size() == 0)
        {
            throw new RuntimeException("modulesMap is empty");
        }

        for (Map.Entry<String, Project> module : modulesMap.entrySet())
        {
            if (module.getValue() != null && !module.getValue().isModuleProject())
            {
                if (project != null)
                {
                    throw new RuntimeException("Several projects were found: " + project + ", " + module);
                }
                else
                {
                    project = module.getValue();
                }
            }
        }

        if (project == null)
        {
            //todo create new not module project for tests?
            logger.info("Project not found, try load main module.");
            project = new ProjectTopologicalSort(modulesMap.values()).getRoot();
        }

        long startTime = System.nanoTime();
        ModuleLoader2.mergeModules(project, logger);
        project.validate();
        project.initBeSqlMacros();
        if (project.hasFeature(BE_SQL_QUERIES))
        {
            processOldFreemarkerMacros(project);
        }
        ModuleLoader2.logLoadedProject(project, startTime, logger);
        return project;
    }

    private static void processOldFreemarkerMacros(Project project)
    {
        List<String> allowedFreemarkerMacros = Arrays.asList("<@_copySelectionQuery/>",
                "<@_copyAllRecordsQuery/>", "<@_copyQuery ");
        for (String entityName : project.getEntityNames())
        {
            for (Query query : project.getEntity(entityName).getQueries())
            {
                if (query.isSqlQuery())
                {
                    String queryCode = query.getQuery().trim();
                    String queryAfterFreemarker = query.getQueryCompiled().validate().trim();
                    if ((queryCode.contains("${") || queryCode.contains("<@")) &&
                            allowedFreemarkerMacros.stream().filter(queryCode::contains).count() == 0)
                    {
                        throw new IllegalArgumentException("Project used " + BE_SQL_QUERIES +
                                " feature, please use be-sql instead freemarker.\n" + "Compile freemarker for query:"
                                + queryCode + "\n==============\n" + queryAfterFreemarker);
                    }
                    query.setQuery(queryAfterFreemarker);
                }
            }
        }
    }

    public static Map<String, Project> getModulesMap()
    {
        return modulesMap;
    }

    public static void clear()
    {
        modulesMap = new HashMap<>();
        pathsToProjectsToHotReload = new HashMap<>();
        devRoles = new ArrayList<>();
    }

    private static synchronized void loadAllProjects(boolean dirty, ProcessController logger)
    {
        loadAllProjects(dirty, Collections.emptyList(), logger);
    }

    private static synchronized void loadAllProjects(boolean dirty, List<URL> additionalUrls, ProcessController logger)
    {
        if (modulesMap != null && !dirty)
            return;

        try
        {
            if (urls == null) urls = getProjectUrls(additionalUrls, logger);
            loadAllProjects(urls, logger);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static List<URL> getProjectUrls(List<URL> additionalUrls, ProcessController logger) throws IOException
    {
        List<URL> list = Collections.list(ModuleLoader2.class.getClassLoader().getResources(
                ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX));
        list.addAll(additionalUrls);
        replaceAndAddURLtoSource(list, logger);
        return list;
    }

    public static void loadAllProjects(List<URL> urls)
    {
        loadAllProjects(urls, new JULLogger(log));
    }

    public static void loadAllProjects(List<URL> urls, ProcessController logger)
    {
        modulesMap = null;
        try
        {
            Map<String, Project> newModulesMap = new HashMap<>();
            for (URL url : urls)
            {
                LoadContext loadContext = new LoadContext();

                Project module;
                String ext = url.toExternalForm();
                if (ext.indexOf('!') < 0) // usual file in directory
                {
                    Path path = Paths.get(url.toURI()).getParent();
                    module = Serialization.load(path, loadContext);
                    logger.debug("Load module from dir: " + path);
                }
                else // war or jar file
                {
                    String jar = ext.substring(0, ext.indexOf('!'));
                    FileSystem fs;

                    try
                    {
                        fs = FileSystems.newFileSystem(URI.create(jar), Collections.emptyMap());
                    }
                    catch (FileSystemAlreadyExistsException e)
                    {
                        fs = FileSystems.getFileSystem(URI.create(jar));
                        logger.debug("Get exists FileSystem after exception");
                    }

                    Path path = fs.getPath("./");
                    module = Serialization.load(path, loadContext);

                    logger.debug("Load module from " + url.toExternalForm() + ", path=" + path);
                }
                loadContext.check();
                newModulesMap.put(module.getAppName(), module);
            }
            modulesMap = newModulesMap;
        }
        catch (ProjectLoadException | IOException | URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean containsModule(String name, ProcessController logger)
    {
        loadAllProjects(false, logger);

        return modulesMap.containsKey(name);
    }

    public static Path getModulePath(String name)
    {
        return getModulePath(name, new JULLogger(log));
    }

    public static Path getModulePath(String name, ProcessController logger)
    {
        loadAllProjects(false, new JULLogger(log));

        return modulesMap.get(name).getLocation();
    }

    public static void addModuleScripts(Project project, ProcessController logger) throws ReadException
    {
        loadAllProjects(false, logger);

        for (Module module : project.getModules())
        {
            Serialization.loadModuleMacros(module);
        }
    }

    public static List<Project> loadModules(Project application, ProcessController logger, LoadContext loadContext)
            throws ProjectLoadException
    {
        List<Project> result = new ArrayList<>();
        for (Module module : application.getModules())
        {
            if (containsModule(module.getName(), logger))
            {
                Project moduleProject = modulesMap.get(module.getName());
                result.add(moduleProject);
            }
            else
            {
                throw new RuntimeException("Module project not found: '" + module.getName() + "'");
            }
        }
        //todo ????? topological sort?
        result.sort((o1, o2) -> {
            if (o1.getModules().contains(o2.getName()))
                return 1;
            if (o2.getModules().contains(o1.getName()))
                return -1;
            return 0;
        });
        return result;
    }

    public static void mergeModules(Project be5Project, ProcessController logger) throws ProjectLoadException
    {
        LoadContext loadContext = new LoadContext();
        try
        {
            ModuleLoader2.mergeAllModules(be5Project, logger, loadContext);
        }
        catch (ProjectLoadException e)
        {
            throw new ProjectLoadException("Merge modules", e);
        }
        loadContext.check();
    }

    private static void mergeAllModules(
            final Project model,
            final ProcessController logger,
            final LoadContext context) throws ProjectLoadException
    {
        mergeAllModules(model, loadModules(model, logger, context), context);
    }

    public static void mergeAllModules(final Project model, List<Project> modules, final LoadContext context)
            throws ProjectLoadException
    {
        modules = new LinkedList<>(modules);

        for (Project module : modules)
        {
            module.mergeHostProject(model);
        }

        final Project compositeModule = foldModules(model, modules, context);
        if (compositeModule != null)
        {
            model.merge(compositeModule);
        }
    }

    private static Project foldModules(final Project model, final List<Project> modules, LoadContext context)
    {
        if (modules.isEmpty())
        {
            return null;
        }

        Project compositeModule = null;

        for (Project module : modules)
        {
            if (compositeModule != null)
            {
                module.getModules().merge(compositeModule.getModules(), true, true);
                module.getApplication().merge(compositeModule.getModule(module.getProjectOrigin()),
                        true, true);
            }

            module.applyMassChanges(context);
            compositeModule = module;

            if (compositeModule.isModuleProject())
            {
                DataElementUtils.addQuiet(module.getModules(), module.getApplication());
                module.setApplication(new Module(model.getProjectOrigin(), model));
            }
        }

        return compositeModule;
    }

    /**
     * Returns BeanExplorerProjectFileSystem for given module if possible
     */
    public static ProjectFileSystem getFileSystem(Project app, String moduleName)
    {
        if (app.getProjectOrigin().equals(moduleName))
        {
            return new ProjectFileSystem(app);
        }
        Path modulePath = ModuleLoader2.getModulePath(moduleName);
        if (modulePath != null)
        {
            Project project = new Project(moduleName);
            project.setLocation(modulePath);
            project.setProjectFileStructure(new ProjectFileStructure(project));
            return new ProjectFileSystem(project);
        }

        return null;
    }

    private static void logLoadedProject(Project project, long startTime, ProcessController logger)
    {
        logger.info("------------------------------------------------------------------------");
        if (project.isModuleProject())
        {
            logger.info("Module      : " + project.getName());
        }
        else
        {
            logger.info("Project     : " + project.getName());
        }

        if (project.getModules().getSize() > 0)
        {
            logger.info("Modules     : " + project.getModules().getNameList().stream()
                    .collect(joining(", ")));
        }
        long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        logger.info("Loading time: " + time + " ms");
        logger.info("------------------------------------------------------------------------");
    }

    /**
     * For hot reload
     *
     * @param urls projects URL
     */
    private static void replaceAndAddURLtoSource(List<URL> urls, ProcessController logger)
    {
        try
        {
            readDevPathsToSourceProjects(logger);
            if (pathsToProjectsToHotReload.isEmpty()) return;

            logger.info("Replace project path for hot reload (dev.yaml):");

            for (Map.Entry<String, Path> moduleSource : pathsToProjectsToHotReload.entrySet())
            {
                boolean used = false;
                for (int i = 0; i < urls.size(); i++)
                {
                    String name = getProjectName(urls.get(i));
                    if (name.equals(moduleSource.getKey()))
                    {
                        used = true;
                        urls.set(i, moduleSource.getValue().resolve("project.yaml").toUri().toURL());
                        logger.info(" - " + String.format("%-20s", name) + urls.get(i) + " - replace");
                    }
                }
                if (!used)
                {
                    URL url = moduleSource.getValue().resolve("project.yaml").toUri().toURL();
                    urls.add(url);
                    logger.info(" - " + moduleSource.getKey() + ": " + url + " - add");
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static String getProjectName(URL url) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                StandardCharsets.UTF_8)))
        {
            Map<String, Object> module = new Yaml().load(reader);
            return module.entrySet().iterator().next().getKey();
        }
    }

    private static void readDevPathsToSourceProjects(ProcessController logger) throws IOException
    {
        ArrayList<URL> urls = Collections.list(ModuleLoader2.class.getClassLoader().getResources("dev.yaml"));
        if (urls.size() > 1)
        {
            logger.error("dev.yaml should be only in the project.");
            throw new RuntimeException("dev.yaml should be only in the project.");
        }

        if (urls.size() == 1)
        {
            readDevPathsToSourceProjects(urls.get(0), logger);
        }
    }

    /**
     * dev.yaml example:
     * paths:
     *   testBe5app: /home/uuinnk/workspace/github/testBe5app
     * roles:
     * - SystemDeveloper
     */
    @SuppressWarnings("unchecked")
    static void readDevPathsToSourceProjects(URL url, ProcessController logger) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                StandardCharsets.UTF_8)))
        {
            Map<String, Object> content = new Yaml().load(reader);

            initPathsForDev(content, logger);
            if (content.get("roles") != null)
            {
                devRoles = (List<String>) content.get("roles");
                logger.info("Dev roles read - " + devRoles.toString());
            }
            else
            {
                devRoles = Collections.emptyList();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void initPathsForDev(Map<String, Object> content, ProcessController logger)
    {
        Map<String, String> paths = (Map<String, String>) content.get("paths");
        if (paths != null)
        {
            for (Map.Entry<String, String> entry : paths.entrySet())
            {
                if (Paths.get(entry.getValue()).resolve("project.yaml").toFile().exists())
                {
                    pathsToProjectsToHotReload.put(entry.getKey(), Paths.get(entry.getValue()));
                }
                else
                {
                    logger.error("Error path in dev.yaml for " + entry.getKey());
                }
            }
        }
    }

    public static Map<String, Path> getPathsToProjectsToHotReload()
    {
        return pathsToProjectsToHotReload;
    }

    public static List<String> getDevRoles()
    {
        return devRoles;
    }

    public static boolean getDevFileExists()
    {
        return ModuleLoader2.class.getClassLoader().getResource("dev.yaml") != null;
    }

}
