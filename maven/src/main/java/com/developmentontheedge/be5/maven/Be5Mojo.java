package com.developmentontheedge.be5.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

///
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.PropertyConfigurator;
import org.yaml.snakeyaml.Yaml;

import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlDeserializer;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlSerializer;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.be5.metadata.util.WriterLogger;
import com.developmentontheedge.beans.model.ComponentFactory;
import com.developmentontheedge.beans.model.ComponentModel;
import com.developmentontheedge.beans.model.Property;
import com.developmentontheedge.dbms.MultiSqlParser;

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

    @Parameter (property = "BE5_LOG_DIR")
    protected String logDirPath;
    
    protected Project beanExplorerProject; // Can be injected to avoid parsing
    public void setBeanExplorerProject( final Project project )
    {
        this.beanExplorerProject = project;
    }
    public Project getBeanExplorerProject()
    {
        return beanExplorerProject;
    }

    protected File logDir;
    public File getLogDir()
    {
        return logDir;
    }
    public void setLogDir( File logDir )
    {
        this.logDir = logDir;
    }
    
    protected boolean useMeta;
    public boolean isUseMeta()
    {
        return useMeta;
    }
    public void setUseMeta( boolean useMeta )
    {
        this.useMeta = useMeta;
    }

    protected String connectionUrl; // Ant input
    public String getConnectionUrl()
    {
        return connectionUrl;
    }
    public void setConnectionUrl( final String connectionUrl )
    {
        this.connectionUrl = connectionUrl;
    }

    protected boolean modules = false;
    public boolean isModules()
    {
        return modules;
    }
    public void setModules( boolean modules )
    {
        this.modules = modules;
    }
   

    ///////////////////////////////////////////////////////////////////    
    
    public void init() // throws MojoFailureException
    {
    	Properties properties = new Properties();

    	// TODO replace to Maven logging
    	properties.setProperty( "log4j.rootCategory", "INFO,stderr" );
        properties.setProperty( "log4j.appender.stderr", "org.apache.log4j.ConsoleAppender" );
        properties.setProperty( "log4j.appender.stderr.Threshold", "INFO" );
        properties.setProperty( "log4j.appender.stderr.Target", "System.err" );
        properties.setProperty( "log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout" );
        properties.setProperty( "log4j.appender.stderr.layout.ConversionPattern", "%-5p %d [%t][%F:%L] : %m%n" );
        PropertyConfigurator.configure( properties );
    }

    protected void initParameters() throws MojoFailureException
    {
        if( debug == true )
        {
            ModuleUtils.setDebugStream( System.err );
        }
        
        if(logDirPath != null)
        {
            logDir = new File(logDirPath);
        }

        getLog().info("BE5 - projectPath: " + projectPath);

        if ( beanExplorerProject == null )
        {
            if ( projectPath == null )
            {
                throw new MojoFailureException( "Please specify projectPath attribute" );
            }
            logger.setOperationName( "Reading project from '" + projectPath + "'..." );
            final Path root = projectPath.toPath();
            this.beanExplorerProject = loadProject( root );
        	applyProfile();
        }
        if(debug)
        {
            beanExplorerProject.setDebugStream( System.err );
        }
    	
        if ( connectionUrl == null )
        {
            String user = null;
            String password = null;
            BeConnectionProfile profile = beanExplorerProject.getConnectionProfile();
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

		// TODO
        //this.connector = DBMSBase.createConnector( connectionUrl );
        this.beanExplorerProject.setDatabaseSystem( DatabaseUtils.getRdbms( connector ) );
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

    protected void applyProfile() throws MojoFailureException
    {
    	/* TODO
        String profileSerialized = getProject().getProperty( "BE4_PROFILE_SERIALIZED" );
        if(profileSerialized != null)
        {
            try
            {
                LoadContext loadContext = new LoadContext();
                final Project proj = beanExplorerProject;
                
                BeConnectionProfile createdProfile = YamlDeserializer.deserializeConnectionProfile( loadContext, profileSerialized, proj );
                if(!loadContext.getWarnings().isEmpty())
                    throw loadContext.getWarnings().get( 0 );
                DataElementUtils.saveQuiet( createdProfile );
                beanExplorerProject.setConnectionProfileName( createdProfile.getName() );
                return;
            }
            catch ( ReadException | ClassCastException e )
            {
                throw new MojoExecutionException("apply profile error: " + e);
            }
        }
        String profileName = getProject().getProperty( "BE4_PROFILE" );
        if(profileName != null)
        {
            beanExplorerProject.setConnectionProfileName( profileName );
            if(beanExplorerProject.getConnectionProfile() == null && !beanExplorerProject.isModuleProject())
            {
                throw new MojoFailureException( "Cannot find connection profile '"+profileName+"'" );
            }
        }*/
    	
        BeConnectionProfile profile = beanExplorerProject.getConnectionProfile();
        if(profile != null)
        {
            setupProfile(profile);
        }
    }

    private void setupProfile(BeConnectionProfile profile) throws MojoFailureException
    {
    	/* TODO
        String[] properties = profile.getPropertiesToRequest();
        if(properties == null)
            return;
        ComponentModel model = ComponentFactory.getModel(profile, ComponentFactory.Policy.DEFAULT);
        for(String propertyName : properties)
        {
            Property property = model.findProperty( propertyName );
            if(property == null)
            {
                throw new MojoFailureException("Error in connection profile '"+profile.getName()+"': unknown property '"+propertyName+"'");
            }
            try
            {
                Object value = property.getValue();
                String propertyValue = getProject().getProperty( "BE4_PROFILE_PROPERTY_"+propertyName.replace( '/', '_' ) );
                if(propertyValue == null)
                {
                    propertyValue = readString(property.getDisplayName(), value == null ? "" : value.toString());
                }
                if(value instanceof Integer)
                {
                    property.setValue( Integer.valueOf( propertyValue ) );
                } else
                {
                    property.setValue( propertyValue );
                }
            }
            catch ( IOException | NumberFormatException | NoSuchMethodException e )
            {
                throw new MojoExecutionException("Connection profile error: ", e);
            }
        }
        profile.setPropertiesToRequest( null );
        LinkedHashMap<String, Object> serializedProfiles = new LinkedHashMap<>();
        serializedProfiles.put( profile.getName(), YamlSerializer.serializeProfile( profile ) );
        String serialized = new Yaml().dump( serializedProfiles );
        setProperty( "BE4_PROFILE_SERIALIZED", serialized );
        */
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

    protected void setProperty(String name, String value)
    {
    	/* TODO
        if(value != null)
        {
            value = getProject().replaceProperties( value );
        }
        if(debug)
        {
            String oldValue = getProject().getProperty( name );
            if ( value == null )
            {
                System.err.println( "BE4 skips null value for property " + name );
            }
            else
            {
                if ( oldValue == null )
                {
                    System.err.println( "BE4 sets " + name + "=" + value.replace( "\n", System.lineSeparator() ) );
                }
                else if ( !oldValue.equals( value ) )
                {
                    System.err.println( "BE4 does not update existing property " + name );
                    System.err.println( "\tExisting value: " + oldValue.replace( "\n", System.lineSeparator() ) );
                    System.err.println( "\tRequested and ignored value: " + value.replace( "\n", System.lineSeparator() ) );
                }
            }
        }
        if(value == null)
            return;
        getProject().setNewProperty( name, value );
        if(!name.startsWith( "BE4_" ) && !name.endsWith( "::extras" ) && value != null && properties.getProperty( name ) == null)
        {
            properties.setProperty( name, getProject().getProperty( name ) );
        }
        */
    }

   
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
