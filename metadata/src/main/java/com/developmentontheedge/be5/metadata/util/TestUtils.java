package com.developmentontheedge.be5.metadata.util;

import java.nio.file.Path;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;

public class TestUtils
{
    public Project loadProject(Path projectDirectory) throws ProjectLoadException
    {
        final LoadContext loadContext = new LoadContext();
        Project prj = Serialization.load( projectDirectory, loadContext );
        if(!loadContext.getWarnings().isEmpty())
        {
            StringBuilder sb = new StringBuilder();
            for(ReadException exception : loadContext.getWarnings())
            {
                sb.append("Error: "+exception.getMessage()).append('\n');
            }
            throw new ProjectLoadException( projectDirectory, new Exception(sb.toString()) );
        }
        return prj;
    }
    
    public DatabaseConnector createConnector(Path projectDirectory, String profileName) throws ProjectLoadException
    {
        Project project = loadProject( projectDirectory );
        project.setConnectionProfileName( profileName );
        return DatabaseUtils.createConnector( project );
    }
}
