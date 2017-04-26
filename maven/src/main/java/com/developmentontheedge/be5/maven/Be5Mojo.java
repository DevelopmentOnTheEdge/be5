package com.developmentontheedge.be5.maven;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.MultiSqlParser;
import com.developmentontheedge.dbms.SimpleConnector;

public abstract class Be5Mojo extends AbstractMojo
{
	protected ProcessController logger = new MavenLogger(getLog());

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
    
    protected Project be5Project; 

    protected String  connectionUrl;

    ///////////////////////////////////////////////////////////////////    
    
   
    protected void init() throws MojoFailureException
    {
    	initLogging();
    	
        if( projectPath == null )
            throw new MojoFailureException("Please specify projectPath attribute");

        getLog().info("Reading be5 project from '" + projectPath + "'...");
        be5Project = loadProject(projectPath.toPath());
        if(debug)
        {
            be5Project.setDebugStream( System.err );
        }
    	
        BeConnectionProfile profile = be5Project.getConnectionProfile();
        if ( connectionUrl != null )
        {
            getLog().info("Using connection " + connectionUrl);
        }
        else
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
                      + be5Project.getProjectFileStructure().getSelectedProfileFile()
                      + " file with profile name or use -DBE5_PROFILE=..." );
            }

            if ( user != null )
            {
                getLog().info("Using connection " + DatabaseUtils.formatUrl(connectionUrl, user, "xxxxx"));
                connectionUrl = DatabaseUtils.formatUrl(connectionUrl, user, password);
            } 
            else
            {
                logger.setOperationName("Using connection "+connectionUrl);
            }
        } 
        
		this.be5Project.setDatabaseSystem( Rdbms.getRdbms(connectionUrl) );

        this.connector = new SimpleConnector(Rdbms.getRdbms(connectionUrl).getType(),
					                             profile.getConnectionUrl(), 
												 profile.getUsername(), profile.getPassword());
    }
    
    /**
     * Configures JUL (java.util.logging).
     */
    protected void initLogging()
    {
    	// configure JUL logging
    	String ln = System.lineSeparator();
    	String level = debug ? "FINEST" : "INFO";
    	String logConfig = 
//    			".level=" + level +
    			"handlers= java.util.logging.ConsoleHandler" + ln +
    			"java.util.logging.ConsoleHandler.level = " + level + ln +
     			"java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter" + ln +
    	        "java.util.logging.SimpleFormatter.format =%1$TT %4$s: %5$s%n";

    	// JUL - String.format(format, date, source, logger, level, message, thrown);
    	//                             1     2       3       4      5        6    
    	
    	try 
    	{
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(logConfig.getBytes()));
        } 
    	catch (IOException e) 
    	{
            System.err.println("Could not setup logger configuration: " + e.toString());
        }    	
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
            ModuleLoader2.mergeAllModules( be5Project, logger, loadContext );
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
        System.err.println( MultiSqlParser.normalize( be5Project.getDatabaseSystem().getType(), ddlString ) );
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
