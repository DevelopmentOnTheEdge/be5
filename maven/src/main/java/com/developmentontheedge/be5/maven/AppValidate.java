package com.developmentontheedge.be5.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugin.MojoExecutionException; 

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.metadata.model.base.BeElementWithProperties;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.ConnectionUrl;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;

/**
 * Usage example: 
 * mvn be5:validate -DBE5_DEBUG=true
 */
@Mojo( name = "validate")
public class AppValidate extends Be5Mojo
{
    @Parameter (property = "BE5_RDBMS")
    protected String rdbmsName; 

    @Parameter (property = "BE5_SKIP_VALIDATION")
    protected boolean skipValidation = false;

    @Parameter (property = "BE5_CHECK_QUERY")
    protected String queryPath;

    @Parameter (property = "BE5_CHECK_ROLES")
    protected boolean checkRoles;
    
    @Parameter (property = "BE5_CHECK_DDL")
    protected String ddlPath;

    @Parameter (property = "BE5_SAVE_PROJECT")
    protected boolean saveProject;
    
    @Override
    public void execute() throws MojoFailureException
    {
        logger.setOperationName( "Reading project from " + projectPath + "..." );
        this.beanExplorerProject = loadProject( projectPath.toPath() );
        applyProfile();

        setRdbms();
        loadModules();
        validateProject();
        checkQuery();
        checkRoles();
        checkDdl();
        saveProject();
//        checkProfileProtection();
//        String propertiesFile = getProject().getProperty( "BE4_CREATE_PROFILE_PROPERTIES" );
//        setupAnt();
//        if(propertiesFile != null)
//        {
//            storeProperties( propertiesFile );
//        }
    }

    private void checkProfileProtection() throws MojoFailureException
    {
        if(beanExplorerProject.getConnectionProfile() != null &&
                beanExplorerProject.getConnectionProfile().isProtected() &&
                ! unlockProtectedProfile )
        {
            System.err.println( "=== WARNING! ===" ); 
            System.err.println( "You are using the protected profile '" + beanExplorerProject.getConnectionProfileName()+"'");
            System.err.println( "The following database may be modified due to this command: " + beanExplorerProject.getConnectionProfile().getConnectionUrl());
            System.err.println( "Type the profile name to confirm its usage:" );
            String line = "";
            try
            {
                line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
            }
            catch ( IOException e )
            {
                // ignore
            }
            if(beanExplorerProject.getConnectionProfileName().equals( line ))
            {
                setProperty( "BE4_UNLOCK_PROTECTED_PROFILE", "true" );
            } else 
            {
                throw new MojoFailureException("Aborted");
            }
        }
    }

    private void loadModules() throws MojoFailureException
    {
        if(!modules)
            return;
        
        LoadContext loadContext = new LoadContext();
        List<ProjectElementException> errors = new ArrayList<>();
        try
        {
            final Project model = beanExplorerProject;
            List<Project> moduleProjects = ModuleUtils.loadModules( model, logger, loadContext );
            errors.addAll( validateDeps(moduleProjects) );
            ModuleUtils.mergeAllModules( model, null, moduleProjects, loadContext );
        }
        catch ( ProjectLoadException e )
        {
            throw new MojoFailureException("Can not load project modules", e);
        }
        checkErrors( loadContext, "Modules have %d error(s)" );
    }

    private void setRdbms()
    {
        // Need to set any system to validate project
        if(rdbmsName != null)
            beanExplorerProject.setDatabaseSystem( Rdbms.valueOf( rdbmsName.toUpperCase(Locale.ENGLISH) ) );
        if(beanExplorerProject.getDatabaseSystem() == null)
        {
            beanExplorerProject.setDatabaseSystem( Rdbms.POSTGRESQL );
        }
    }

    private void validateProject() throws MojoFailureException
    {
        List<ProjectElementException> errors = new ArrayList<>();
        if( skipValidation )
        {
            logger.setOperationName( "Validation skipped" );
        } else
        {
            logger.setOperationName( "Validating..." );
            errors.addAll( beanExplorerProject.getErrors() );
            int count = 0;
            for(ProjectElementException error : errors)
            {
                if(error.getPath().equals( beanExplorerProject.getName() ) && error.getProperty().equals( "connectionProfileName" ))
                    continue;
                count++;
                displayError( error );
            }
            if(count > 0)
            {
                throw new MojoFailureException( "Project has "+count+" errors" );
            }
            logger.setOperationName( "Project is valid." );
            setProperty( "BE4_SKIP_VALIDATION", "true" );
        }
    }

    private List<ProjectElementException> validateDeps( List<Project> moduleProjects )
    {
        List<ProjectElementException> moduleErrors = new ArrayList<>();
        Map<String, String> entityToModule = new HashMap<>();
        for(Project prj : moduleProjects)
        {
            for(Entity entity : prj.getApplication().getEntities())
                entityToModule.put( entity.getName(), prj.getName() );
        }
        for(Project prj : moduleProjects)
        {
            for(Entity entity : prj.getApplication().getEntities())
            {
                for(TableReference ref : entity.getAllReferences())
                {
                    String moduleTo = entityToModule.get( ref.getTableTo() );
                    if(moduleTo != null && prj.getModule( moduleTo ) == null)
                    {
                        moduleErrors.add( new ProjectElementException( ref, "Reference to entity '" + ref.getTableTo()
                            + "' which is defined in module '" + moduleTo + "' which is not specified in dependencies of module '"
                            + prj.getName() + "'" ) );
                    }
                }
            }
        }
        return moduleErrors;
    }

    
    private void saveProject() throws MojoFailureException
    {
        if( saveProject )
        {
            try
            {
                logger.setOperationName( "Saving..." );
                Serialization.save( beanExplorerProject, beanExplorerProject.getLocation() );
            }
            catch(ProjectSaveException e)
            {
                throw new MojoFailureException("Can not save project.", e);
            }
        }
    }
/*
    private void storeProperties( String propertiesFile )
    {
        File file = getProject().resolveFile( propertiesFile );
        logger.setOperationName( "Writing "+file );
        StringWriter sw = new StringWriter();
        try
        {
            properties.store( sw, "Generated by BE4 from project '"+beanExplorerProject.getName()+"' using profile '"+beanExplorerProject.getConnectionProfileName()+"'" );
            String[] strings = sw.toString().split( System.lineSeparator() );
            Arrays.sort( strings );
            Files.write( file.toPath(), Arrays.asList( strings ), StandardCharsets.UTF_8 );
        }
        catch ( IOException e )
        {
            throw new MojoFailureException( e );
        }
    }
*/
    private void checkDdl() throws MojoFailureException
    {
        if( ddlPath != null)
        {
            Entity entity = beanExplorerProject.getEntity(ddlPath);
            if(entity == null)
            {
                throw new MojoFailureException("Invalid entity: " +  ddlPath);
            }
            DdlElement scheme = entity.getScheme();
            if(scheme == null)
            {
                throw new MojoFailureException("Entity has no scheme: " + ddlPath);
            }
            System.err.println( scheme.getDdl().replaceAll( "\n", System.lineSeparator() ) );
        }
    }
    
    private void checkRoles()
    {
        if( checkRoles )
        {
            System.err.println( "Available roles:\n" + String.join( System.lineSeparator(), beanExplorerProject.getAvailableRoles() ) );
        }
    }

    private void checkQuery() throws MojoFailureException
    {
        if( queryPath == null)
            return;
        
        int pos = queryPath.indexOf( '.' );
        if(pos <= 0)
        {
            throw new MojoFailureException("Invalid query path supplied: " + queryPath);
        }
        String entityName = queryPath.substring( 0, pos );
        String queryName  = queryPath.substring( pos+1 );
        Entity entity = beanExplorerProject.getEntity( entityName );
        if(entity == null)
        {
            throw new MojoFailureException("Invalid entity: " + entityName);
        }
        Query query = entity.getQueries().get( queryName );
        if(query == null)
        {
            try
            {
                queryName = new String(queryName.getBytes( "CP866" ), "CP1251");
                query = entity.getQueries().get( queryName );
            }
            catch(UnsupportedEncodingException e)
            {
                throw new MojoFailureException("Can not load query, path=" + queryPath, e);
            }
        }
        if(query == null)
        {
            throw new MojoFailureException("Invalid query: "+queryName);
        }
        System.err.println( query.getQueryCompiled().getResult().replaceAll( "\n", System.lineSeparator() ) );
    }

/*    
    protected void setupAnt()
    {
        List<String> moduleNames = new ArrayList<>();
        for(Module module : beanExplorerProject.getModules())
        {
            if(!ModuleUtils.SYSTEM_MODULE.equals( module.getName()))
                moduleNames.add( module.getName() );
            String[] extras = module.getExtras();
            if(extras != null && extras.length != 0)
            {
                String propertyName = module.getName() + "::extras";
                setProperty( propertyName, String.join( ",", extras ) );
            }
            if(ModuleUtils.isModuleExist( module.getName() ))
            {
                setProperty( "BE4_"+module.getName(), getProject().getProperty( "BE4_MODULES" ) );
            }
        }
        setProperty( "MODULES", String.join( ",", moduleNames ) );
        setProperty( "FEATURES", String.join( ",", beanExplorerProject.getFeatures() ) );
        BeConnectionProfile profile = beanExplorerProject.getConnectionProfile();
        if(profile == null && !beanExplorerProject.isModuleProject())
        {
            profile = createProfile();
        }
        if(profile != null)
        {
            try
            {
                ConnectionUrl url = new ConnectionUrl( profile.getConnectionUrl() );
                setProperty( "USER", profile.getUsername() );
                setProperty( "PASS", profile.getPassword() );
                setProperty( "db_platform", url.getRdbms().getAntName() );
                setProperty( "DBMS", url.getRdbms().getAntName() );
                setProperty( "DB_PORT", String.valueOf(url.getPort()) );
                setProperty( "tcpip.port", String.valueOf(url.getPort()) );
                setProperty( "DB_HOST", url.getHost() );
                if(url.getProperty( "SID" ) != null)
                    setProperty( "SID", url.getProperty( "SID" ) );
                setProperty( "DATABASE", url.getDb() );
            }
            catch ( MojoFailureException e )
            {
                throw e;
            }
            catch ( RuntimeException e )
            {
                throw new MojoFailureException( e );
            }
            setProperty( "PROJECT_NAME", profile.getRealTomcatAppName() );
            setAntProperties( profile );
        }
        setAntProperties( beanExplorerProject );
        setProperty( "db_platform.version", beanExplorerProject.getDatabaseSystem().getDefaultVersion() );
    }

    private BeConnectionProfile createProfile()
    {
        try
        {
            System.err.println("Please specify database connection settings:");
            String dbms = readString("DBMS", Rdbms.POSTGRESQL.toString().toLowerCase(), (Object[])Rdbms.values());
            Rdbms rdbms = Rdbms.valueOf( dbms.toUpperCase() );
            String host = readString("Host", "localhost");
            int port = Integer.parseInt( readString( "Port", String.valueOf( rdbms.getDefaultPort() ) ) );
            String database = readString("Database", beanExplorerProject.getName());
            String user = readString("Username", database);
            String password = readString("Password", user);
            BeConnectionProfile profile = new BeConnectionProfile( "user-defined", beanExplorerProject.getConnectionProfiles().getLocalProfiles() );
            profile.setUsername( user );
            profile.setPassword( password );
            profile.setConnectionUrl( rdbms.createConnectionUrl(host, port, database, Collections.<String, String>emptyMap()) );
            if(rdbms == Rdbms.POSTGRESQL)
            {
                profile.setProperty( "db_platform.version", "91" );
            }
            return profile;
        }
        catch ( MojoFailureException e )
        {
            throw e;
        }
        catch ( RuntimeException | IOException e )
        {
            throw new MojoFailureException( e );
        }
    }

    protected void setAntProperties( BeElementWithProperties element )
    {
        for ( String property : element.getPropertyNames() )
        {
            setProperty( property, element.getProperty( property ) );
        }
    }
*/    
}
