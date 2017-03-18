package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.ServletContexts;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProjectProviderImpl implements ProjectProvider
{
    private Logger log = Logger.getLogger(ProjectProviderImpl.class.getName());
    private volatile boolean dirty = false;
    private WatchDir watcher = null;
    private Project project;
    
    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
		    long time = System.nanoTime();
			project = loadProject();
            log.info("Loading project took "+TimeUnit.NANOSECONDS.toMillis( System.nanoTime()-time )+" ms");
    	}

    	return project;
    }
    
    public Path getPath(ServletContext ctx, String attributeName)
    {
        String projectSource = ctx.getInitParameter( attributeName );
        if(projectSource == null || projectSource.equals( "db") )
        {
            return null;
        }
        if(projectSource.startsWith( "war:" ))
        {
            projectSource = ctx.getRealPath( projectSource.substring( "war:".length() ) );
        }
        return Paths.get(projectSource);
    }

    private Project loadProject() {
        try
        {
            if(watcher != null)
                watcher.stop();
            ServletContext ctx = ServletContexts.getServletContext();
            Path projectSource = getPath( ctx, "be5.projectSource" );
            Path modulesSource = getPath( ctx, "be5.modulesSource" );
            Path auxModulesSource = getPath( ctx, "be5.auxModulesSource" );
            if(modulesSource == null)
            {
                throw Be5Exception.internal( "be5.modulesSource is not specified in web.xml" );
            }
            return loadProjectFromYaml( projectSource, modulesSource, auxModulesSource );
        }
        finally
        {
            dirty = false;
        }
    }

    protected Project loadProjectFromYaml(Path projectSource, Path modulesSource, Path auxModulesSource)
    {
//TODO        logger.info("Loading BE4 project from YAML ["+projectSource+"]");
//        ModuleUtils.setBasePathProvider( new BasePathProvider()
//        {
//            @Override
//            public Path getBasePath()
//            {
//                return modulesSource;
//            }
//
//            @Override
//            public Path evalBasePath()
//            {
//                return modulesSource;
//            }
//
//            @Override
//            public void basePathGuessed(Path basePath)
//            {
//            }
//        } );
//        ModuleUtils.setAdditionalModulePaths( auxModulesSource == null ? Collections.emptyList() : Arrays.asList( auxModulesSource ) );
        LoadContext loadContext = new LoadContext();
        try
        {
            Project project = Serialization.load( projectSource, loadContext );
//            Project metaProject = ModuleUtils.loadMetaProject( loadContext );
//            ModuleUtils.mergeAllModules( project, metaProject, new NullLogger(), loadContext );
            loadContext.check();
            watcher = new WatchDir( project ).onModify( path -> {
                dirty = true;
            } ).start();
            return project;
        }
        catch( ProjectLoadException | IOException e )
        {
            throw Be5Exception.internal( e, "Unable to load project from "+projectSource );
        }
    }
}
