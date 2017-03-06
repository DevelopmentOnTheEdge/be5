package com.developmentontheedge.be5.api.services.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Logger;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.caches.Cache;
import com.developmentontheedge.be5.caches.CacheFactory;
import com.developmentontheedge.be5.env.ServletContexts;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ModuleUtils.BasePathProvider;
import com.developmentontheedge.dbms.ExtendedSqlException;

public class ProjectProviderImpl implements ProjectProvider
{
    private final DbmsConnector connector;
    private final Cache projectCache = CacheFactory.getCacheInstance("metadata_be5");
    private volatile boolean dirty = false;
    private WatchDir watcher = null;
    private final Logger logger;
    
    public ProjectProviderImpl(DatabaseService databaseService, Logger logger)
    {
        this.connector = databaseService.getDbmsConnector();
        this.logger = logger;
    }
    
    @Override
    public Project getProject()
    {
    	Project project = (Project) projectCache.get("project");
    	if(dirty || project == null)
    	{
    		synchronized(projectCache)
    		{
    			project = (Project) projectCache.get("project");
    			if(dirty || project == null)
    			{
    			    long time = System.nanoTime();
    				project = loadProject();
    				logger.info("Loading project took "+TimeUnit.NANOSECONDS.toMillis( System.nanoTime()-time )+" ms");
    				projectCache.put("project", project);
    			}
    		}
    	}
        return project;
    }
    
    public static Path getPath(ServletContext ctx, String attributeName)
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
        logger.info("Loading BE4 project from YAML ["+projectSource+"]");
        ModuleUtils.setBasePathProvider( new BasePathProvider()
        {
            @Override
            public Path getBasePath()
            {
                return modulesSource;
            }
            
            @Override
            public Path evalBasePath()
            {
                return modulesSource;
            }
            
            @Override
            public void basePathGuessed(Path basePath)
            {
            }
        } );
        ModuleUtils.setAdditionalModulePaths( auxModulesSource == null ? Collections.emptyList() : Arrays.asList( auxModulesSource ) );
        LoadContext loadContext = new LoadContext();
        try
        {
            Project project = Serialization.load( projectSource, loadContext );
            Project metaProject = ModuleUtils.loadMetaProject( loadContext );
            ModuleUtils.mergeAllModules( project, metaProject, new NullLogger(), loadContext );
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
