package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.env.ServletContexts;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import com.developmentontheedge.be5.servlet.MainServlet;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProjectProviderImpl implements ProjectProvider
{
    private Logger log = Logger.getLogger(ProjectProviderImpl.class.getName());
    
    private static String PROJECT_FILE_NAME = ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX;
    private Project project;

    private WatchDir watcher = null;
    private volatile boolean dirty = false;
    
    @Override
    synchronized public Project getProject()
    {
    	if(dirty || project == null)
    	{
			project = loadProject();
    	}

    	return project;
    }

    private Project loadProject() 
    {
        log.info("Loading project ...");
	    long time = System.nanoTime();

        try
        {
            if(watcher != null)
                watcher.stop();

            // init project path  
            Path path = findProjectPath();

            LoadContext loadContext = new LoadContext();
            Project project = Serialization.load(path, loadContext );
//          ModuleUtils.mergeAllModules( project, metaProject, new NullLogger(), loadContext );
            loadContext.check();
            
            // TODO - check
            watcher = new WatchDir(project).onModify( onModify -> {
                dirty = true;
            } ).start();

            log.info("Project loaded, name='" + project.getName() + "', loading time: " + TimeUnit.NANOSECONDS.toMillis( System.nanoTime()-time ) + " ms");
            return project;
        }
        catch(Throwable t)
        {
        	log.severe("Can not load project, error: " + t.getMessage());
        	throw Be5Exception.internal(t, "Can not load project.");
        }
        finally
        {
            dirty = false;
        }
    }

    protected Path findProjectPath() throws IOException, URISyntaxException
    {
        // try to  find project in classpath or war
        ArrayList<URL> urls = Collections.list((ProjectProviderImpl.class).getClassLoader().getResources(PROJECT_FILE_NAME));
        
        if( urls.isEmpty() )
            throw Be5Exception.internal("Project is not found in classpath or war file.");
        	
        if( urls.size() > 1 )
        {
        	String ln = System.lineSeparator(); 
        	StringBuffer sb = new StringBuffer("Several projects were found: +").append(ln);
        	
        	for(URL url : urls)
        	{
        		sb.append("  - ")
        		  .append(url)
        		  .append(ln);
        	}
        	
        	log.severe(sb.toString());
        	throw Be5Exception.internal(sb.toString());
        }

        // init project path  
        Path path;
        URL url = urls.get(0);
        String ext = url.toExternalForm();

        if( ext.indexOf('!') < 0 ) // usual file in directory
        {
        	path = Paths.get(url.toURI());
        }
        else // war or jar file
        {
        	path = null;
/*            
            String jar = ext.substring(0, ext.indexOf('!'));
            FileSystem fs = FileSystems.newFileSystem(URI.create(jar), new HashMap<String, String>());
            Path p = fs.getPath("./");
            System.out.println("ext=" + url.toExternalForm() + ", path=" + p);                
*/            
        }
        
        return path;
    }
    
    
}
