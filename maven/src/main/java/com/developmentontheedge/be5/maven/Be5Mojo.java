package com.developmentontheedge.be5.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.PropertyConfigurator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.be5.metadata.util.WriterLogger;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.MultiSqlParser;
import com.developmentontheedge.dbms.SimpleConnector;

public abstract class Be5Mojo extends AbstractMojo
{
    protected ProcessController logger = new WriterLogger();
    protected Properties properties = new Properties();
    protected DbmsConnector connector;
    
    ///////////////////////////////////////////////////////////////////
    // Properties
    //
    @Parameter (property = "BE5_PROJECT_PATH", defaultValue = "./")
    protected File projectPath; 

    @Parameter (property = "BE5_UNLOCK_PROTECTED_PROFILE")
    protected boolean unlockProtectedProfile = false;
    
    @Parameter (property = "BE5_DEBUG")
    protected boolean debug = true; //false;

    @Parameter (property = "BE5_LOG_PATH")
    protected File logPath;
    
    protected Project beanExplorerProject; // Can be injected to avoid parsing
    public void setBeanExplorerProject( final Project project )
    {
        this.beanExplorerProject = project;
    }
    public Project getBeanExplorerProject()
    {
        return beanExplorerProject;
    }

    protected boolean useMeta;
    protected String  connectionUrl; // Ant input
    protected boolean modules = false;

    ///////////////////////////////////////////////////////////////////    
    
    protected void init() throws MojoFailureException
    {
    	// TODO replace to Maven logging
    	Properties properties = new Properties();
    	properties.setProperty( "log4j.rootCategory", "INFO,stderr" );
        properties.setProperty( "log4j.appender.stderr", "org.apache.log4j.ConsoleAppender" );
        properties.setProperty( "log4j.appender.stderr.Threshold", "INFO" );
        properties.setProperty( "log4j.appender.stderr.Target", "System.err" );
        properties.setProperty( "log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout" );
        properties.setProperty( "log4j.appender.stderr.layout.ConversionPattern", "%-5p %d [%t][%F:%L] : %m%n" );
        PropertyConfigurator.configure( properties );

        
        getLog().info("BE5 - projectPath: " + projectPath);
        if( projectPath == null )
        {
            throw new MojoFailureException("Please specify projectPath attribute");
        }
        logger.setOperationName("Reading project from '" + projectPath + "'...");
        beanExplorerProject = loadProject(projectPath.toPath());
        if(debug)
        {
            beanExplorerProject.setDebugStream( System.err );
        }
    	
        BeConnectionProfile profile = beanExplorerProject.getConnectionProfile();
        if ( connectionUrl == null )
        {
            String user = null;
            String password = null;
            if ( profile != null )
            {
                connectionUrl = profile.getConnectionUrl();
                user = profile.getUsername();
                password = profile.getPassword();
            }
            if ( connectionUrl == null )
            {
                throw new MojoFailureException(
                        "Please specify connection profile: either create "
                      + beanExplorerProject.getProjectFileStructure().getSelectedProfileFile()
                      + " file with profile name or use -DBE5_PROFILE=..." );
            }
            if ( user != null )
            {
                logger.setOperationName("Using connection "+DatabaseUtils.formatUrl( connectionUrl, user, "xxxxx" ));
                connectionUrl = DatabaseUtils.formatUrl( connectionUrl, user, password );
            } else
            {
                logger.setOperationName("Using connection "+connectionUrl);
            }
        } else
        {
            logger.setOperationName("Using connection "+connectionUrl);
        }
System.out.println("!!connect=" + connectionUrl);

		this.beanExplorerProject.setDatabaseSystem( Rdbms.getRdbms(connectionUrl) );

        this.connector = new SimpleConnector(Rdbms.getRdbms(connectionUrl).getType(),
					                             profile.getConnectionUrl(), 
												 profile.getUsername(), profile.getPassword());
    }
    
    
   
    protected Project loadProject(final Path root) throws MojoFailureException
    {
        final LoadContext loadContext = new LoadContext();
        Project prj;
        try
        {
            prj = Serialization.load(root, loadContext);
        }
        catch(final ProjectLoadException e)
        {
            throw new MojoFailureException("Can not load project", e);
        }
        checkErrors( loadContext, "Project has %d error(s)" );
        return prj;
    }

    protected void mergeModules() throws MojoFailureException
    {
        LoadContext loadContext = new LoadContext();
        try
        {
            Project metaModule = useMeta ? ModuleUtils.loadMetaProject( loadContext )
                : null;
            ModuleUtils.mergeAllModules( beanExplorerProject, metaModule, logger, loadContext );
        }
        catch(ProjectLoadException e)
        {
            throw new MojoFailureException("Merge modules", e);
        }
        if(!loadContext.getWarnings().isEmpty())
        {
            for(ReadException exception : loadContext.getWarnings())
            {
                System.err.println( "Error: "+exception.getMessage() );
            }
            throw new MojoFailureException( "Modules have " + loadContext.getWarnings().size() + " error(s)" );
        }
    }

    ///////////////////////////////////////////////////////////////////    
    
    protected void displayError(ProjectElementException error)
    {
        error.format( System.err );
    }

    protected void checkErrors(final LoadContext loadContext, String messageTemplate) throws MojoFailureException
    {
        if(!loadContext.getWarnings().isEmpty())
        {
            for(ReadException exception : loadContext.getWarnings())
            {
                if(debug)
                {
                    exception.printStackTrace();
                } else
                {
                    System.err.println( "Error: "+exception.getMessage() );
                }
            }
            throw new MojoFailureException( messageTemplate.replace( "%d", String.valueOf( loadContext.getWarnings().size() ) ) );
        }
    }
    

    protected void dumpSql( String ddlString )
    {
        System.err.println( MultiSqlParser.normalize( beanExplorerProject.getDatabaseSystem().getType(), ddlString ) );
    }

    ///////////////////////////////////////////////////////////////////    
   
    protected String readString( String prompt, String defaultValue, Object... values ) throws IOException
    {
        StringBuilder fullPrompt = new StringBuilder(prompt);
        Set<String> vals = new TreeSet<>();
        if(values != null)
        {
            for(Object value : values)
                vals.add( value.toString().toLowerCase() );
        }
        if(!vals.isEmpty())
        {
            fullPrompt.append( " (" ).append( String.join( ", ", vals ) );
            if(defaultValue != null)
            {
                fullPrompt.append( "; default: " ).append( defaultValue );
            }
            fullPrompt.append(")");
        } else if(defaultValue != null)
        {
            fullPrompt.append( "(default: " ).append( defaultValue ).append( ")" );
        }
        fullPrompt.append( ": " );
        String result = "";
        do
        {
            System.err.println( fullPrompt );
            String line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            result = line == null ? "" : line.trim();
            if(result.isEmpty() && defaultValue != null)
                result = defaultValue;
        } while(!vals.isEmpty() && !vals.contains( result ));
        return result;
    }

}
