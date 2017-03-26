package com.developmentontheedge.be5.metadata.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.ProjectFileSystem;
import com.developmentontheedge.be5.metadata.serialization.Serialization;

public class ModuleUtils
{
    public static final String SYSTEM_MODULE = "beanexplorer";
    public static final String SYSTEM_MODULE_SERIALIZED = "beanexplorer_meta";

    public interface BasePathProvider
    {
        /**
         * Called only when it is impossible to guess the base path. The result
         * value can be null. It means that the provider failed to evaluate the
         * base path too.
         */
        Path evalBasePath();

        /**
         * Called when there is no need to use the base path provider.
         */
        void basePathGuessed( Path basePath );

        /**
         * Returns a current base path value.
         */
        Path getBasePath();
    }

    /**
     * Base path provider will be used only if it is impossible to guess the
     * base path.
     */
    public static void setBasePathProvider( BasePathProvider basePathProvider )
    {
        ModuleUtils.basePathProvider = basePathProvider;
    }

    public static void setAdditionalModulePaths( Collection<Path> paths )
    {
        moduleLoader = new ModuleLoader( paths );
    }

    private static BasePathProvider basePathProvider;
    private static volatile Path basePath;
    private static volatile ModuleLoader moduleLoader = new ModuleLoader( Collections.emptyList() );

    public static Path getBasePath()
    {
        if ( basePath != null )
        {
            if ( basePathProvider != null )
            {
                // changed outside
                final Path providedBasePath = basePathProvider.getBasePath();
                if ( providedBasePath != null && !providedBasePath.equals( basePath ) )
                    basePath = providedBasePath;
            }

            return basePath;
        }

        if ( basePathProvider != null )
            basePathProvider.basePathGuessed( basePath );

        return basePath;
    }

    public static Path getLegacyBeanExplorerPath()
    {
        // We assume that legacy BeanExplorer is located on the same level as
        // be4
        return getBasePath().getParent().resolve( "BeanExplorer" );
    }

    public static Path getBeanExplorerIconsPath()
    {
        return getLegacyBeanExplorerPath().resolve( "src" ).resolve( "html" ).resolve( "icons" );
    }

    /**
     * Return path to given BE4-module in default location. Use this mainly to
     * create new modules (using wizard, etc.)
     * 
     * @param moduleName
     *            name of the module
     * @return path to the module
     */
    public static Path getDefaultModulePath( String moduleName )
    {
        return getBasePath().resolve( "modules" ).resolve( moduleName );
    }

    /**
     * Returns path for the BE4 module if it exists
     * 
     * @param moduleName
     *            module name
     * @return path if it exists
     * @throws IllegalArgumentException
     *             if module does not exist
     */
    public static Path getModulePath( String moduleName )
    {
        return resolveModule( moduleName ).orElseThrow( ( ) -> new IllegalArgumentException( "Module not found: " + moduleName ) );
    }

    /**
     * Returns path for the BE4 module if it exists
     * 
     * @param moduleName
     *            module name
     * @return path if it exists, empty optional otherwise
     */
    public static Optional<Path> resolveModule( String moduleName )
    {
        return moduleLoader.resolveModule( moduleName );
    }

    public static Path getLegacyModulePath( String moduleName )
    {
        return getLegacyBeanExplorerPath().resolve( "modules" ).resolve( moduleName );
    }

    public static boolean isModuleExist( String moduleName )
    {
        return moduleLoader.isModuleExists( moduleName );
    }

    public static boolean isLegacyModuleExist( String moduleName )
    {
        Path path = getLegacyModulePath( moduleName );
        return Files.isDirectory( path );
    }

    public static Project loadModule( String moduleName, LoadContext context ) throws ProjectLoadException
    {
        return ModuleLoader2.loadModule( moduleName, context );
    }

    private static Set<String> getModuleList( Path basePath )
    {
        Set<String> result = new TreeSet<>();
        for ( File file : basePath.resolve( "modules" ).toFile().listFiles() )
        {
            if ( file.isDirectory() )
            {
                result.add( file.getName() );
            }
        }
        return result;
    }

    public static Set<String> getAvailableModules()
    {
        return moduleLoader.moduleNames().toSet();
    }

    public static Set<String> getAvailableLegacyModules()
    {
        return getModuleList( getLegacyBeanExplorerPath() );
    }

    public static Set<String> getAvailableFeatures()
    {
        Set<String> features = new TreeSet<>();
        for ( File file : getLegacyBeanExplorerPath().resolve( "src" ).resolve( "sql" ).resolve( "common" ).resolve( "features" ).toFile()
                .listFiles() )
        {
            String name = file.getName();
            if ( !name.endsWith( ".m4" ) )
                continue;
            name = name.substring( 0, name.length() - ".m4".length() );
            int pos = name.indexOf( '_' );
            if ( pos > 0 )
            {
                name = name.substring( 0, pos );
            }
            features.add( name );
        }
        return features;
    }

    public static void addModuleScripts( Project project ) throws ReadException
    {
        for ( Module module : project.getModules() )
        {
            Serialization.loadModuleMacros( module );
        }
    }

    public static List<Project> loadModules( Project application, ProcessController logger, LoadContext loadContext ) throws ProjectLoadException
    {
        List<Project> result = new ArrayList<>();
        for ( Module module : application.getModules() )
        {
            if ( ModuleLoader2.containsModule(module.getName()) )
            {
                if ( logger != null )
                {
                    logger.setOperationName( "Reading module " + module.getName() + "..." );
                }
                Project moduleProject = loadModule( module.getName(), loadContext );
                result.add( moduleProject );
            }
        }
        Collections.sort( result, ( o1, o2 ) -> {
            if ( o1.getModules().contains( o2.getName() ) )
                return 1;
            if ( o2.getModules().contains( o1.getName() ) )
                return -1;
            return 0;
        } );
        return result;
    }

    /**
     * 
     * @param model
     * @throws ProjectLoadException
     */
    public static void mergeAllModules(
        final Project model,
        final ProcessController logger,
        final LoadContext context ) throws ProjectLoadException
    {
        mergeAllModules(model, loadModules(model, logger, context), context);
    }

    /**
     * 
     * @param model
     * @throws ProjectLoadException
     */
    public static void mergeAllModules( final Project model, List<Project> modules, final LoadContext context ) throws ProjectLoadException
    {
        modules = new LinkedList<>( modules );

        for ( Project module : modules )
        {
            module.mergeHostProject( model );
        }

        final Project compositeModule = foldModules( modules, context );
        if ( compositeModule != null )
        {
            model.merge( compositeModule );
        }
    }

    public static void mergeModulesForLocale(
        final Project model,
        final Project projectFromDb,
        final ProcessController logger,
        final LoadContext context ) throws ProjectLoadException
    {
        DataElementUtils.saveQuiet( new BeVectorCollection<>( Project.MODULES, Module.class, model, true ) );
        model.merge( projectFromDb );
        final List<Project> modules = loadModules( model, logger, context );
        for ( Project module : modules )
        {
            Module projectModule = model.getModule( module.getName() );
            if ( projectModule == null )
            {
                projectModule = new Module( module.getName(), model.getModules() );
                DataElementUtils.saveQuiet( projectModule );
            }
            projectModule.getLocalizations().merge( module.getApplication().getLocalizations(), false, false );
        }
    }

    public static Project foldModules( final List<Project> modules, LoadContext context )
    {
        if ( modules.isEmpty() )
        {
            return null;
        }

        Project compositeModule = null;

        for ( Project module : modules )
        {
            if ( compositeModule == null )
            {
                module.applyMassChanges( context );
                compositeModule = module;
            }
            else
            {
                module.getModules().merge( compositeModule.getModules(), true, false );
                module.getApplication().merge( compositeModule.getModule( module.getProjectOrigin() ), true, false );
                module.applyMassChanges( context );
                compositeModule = module;
            }
            if ( compositeModule.isModuleProject() )
            {
                DataElementUtils.addQuiet( module.getModules(), module.getApplication() );
                module.setApplication( null );
            }
        }

        return compositeModule;
    }

    /**
     * Returns BeanExplorerProjectFileSystem for given module if possible
     */
    public static ProjectFileSystem getFileSystem( Project app, String moduleName )
    {
        if ( app.getProjectOrigin().equals( moduleName ) )
        {
            return new ProjectFileSystem( app );
        }
        Path modulePath = moduleLoader.resolveModule( moduleName ).orElse( null );
        if ( modulePath != null )
        {
            Project project = new Project( moduleName );
            project.setLocation( modulePath );
            project.setProjectFileStructure( new ProjectFileStructure( project ) );
            return new ProjectFileSystem( project );
        }
        if ( isLegacyModuleExist( moduleName ) )
        {
            // Works for js forms only
            modulePath = getLegacyModulePath( moduleName );
            Project project = new Project( moduleName );
            project.setLocation( modulePath );
            ProjectFileStructure pfs = new ProjectFileStructure( project );
            pfs.setJsFormsDir( "src/jsforms" );
            project.setProjectFileStructure( pfs );
            return new ProjectFileSystem( project );
        }
        return null;
    }

    public static Project loadMetaProject( LoadContext ctx ) throws ProjectLoadException
    {
        Project metaModule = loadModule( SYSTEM_MODULE_SERIALIZED, ctx );
        Project metaProject = new Project( SYSTEM_MODULE_SERIALIZED );
        DataElementUtils.save( metaModule.getApplication().clone( metaProject.getModules(), SYSTEM_MODULE ) );
        return metaProject;
    }
}
