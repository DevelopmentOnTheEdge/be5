package com.developmentontheedge.be5.mojo;

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

import org.apache.tools.ant.BuildException;

import com.beanexplorer.enterprise.metadata.exception.ProjectElementException;
import com.beanexplorer.enterprise.metadata.exception.ProjectLoadException;
import com.beanexplorer.enterprise.metadata.exception.ProjectSaveException;
import com.beanexplorer.enterprise.metadata.model.BeConnectionProfile;
import com.beanexplorer.enterprise.metadata.model.DdlElement;
import com.beanexplorer.enterprise.metadata.model.Entity;
import com.beanexplorer.enterprise.metadata.model.Module;
import com.beanexplorer.enterprise.metadata.model.Project;
import com.beanexplorer.enterprise.metadata.model.Query;
import com.beanexplorer.enterprise.metadata.model.TableReference;
import com.beanexplorer.enterprise.metadata.model.base.BeElementWithProperties;
import com.beanexplorer.enterprise.metadata.serialization.LoadContext;
import com.beanexplorer.enterprise.metadata.serialization.Serialization;
import com.beanexplorer.enterprise.metadata.sql.ConnectionUrl;
import com.beanexplorer.enterprise.metadata.sql.Rdbms;
import com.beanexplorer.enterprise.metadata.util.ModuleUtils;

public class AppValidate extends BETask
{
    @Override
    public void execute() throws BuildException
    {
        if(isProperty( "BE4_DEBUG" ))
        {
            debug = true;
            ModuleUtils.setDebugStream( System.err );
        }
        if(projectPath == null)
        {
            throw new BuildException("Please specify projectPath attribute");
        }
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
        checkProfileProtection();
        String propertiesFile = getProject().getProperty( "BE4_CREATE_PROFILE_PROPERTIES" );
        setupAnt();
        if(propertiesFile != null)
        {
            storeProperties( propertiesFile );
        }
    }

    private void checkProfileProtection()
    {
        if(beanExplorerProject.getConnectionProfile() != null &&
                beanExplorerProject.getConnectionProfile().isProtected() &&
                !"true".equals( getProject().getProperty( "BE4_UNLOCK_PROTECTED_PROFILE" ) ))
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
                throw new BuildException( "Aborted" );
            }
        }
    }

    private void loadModules()
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
            throw new BuildException( e );
        }
        checkErrors( loadContext, "Modules have %d error(s)" );
    }

    private void setRdbms()
    {
        // Need to set any system to validate project
        String rdbmsName = getProject().getProperty( "BE4_RDBMS" );
        if(rdbmsName != null)
            beanExplorerProject.setDatabaseSystem( Rdbms.valueOf( rdbmsName.toUpperCase(Locale.ENGLISH) ) );
        if(beanExplorerProject.getDatabaseSystem() == null)
        {
            beanExplorerProject.setDatabaseSystem( Rdbms.POSTGRESQL );
        }
    }

    private void validateProject()
    {
        List<ProjectElementException> errors = new ArrayList<>();
        if(isProperty( "BE4_SKIP_VALIDATION" ))
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
                throw new BuildException( "Project has "+count+" errors" );
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

    private void saveProject()
    {
        if(isProperty( "BE4_SAVE" ))
        {
            try
            {
                logger.setOperationName( "Saving..." );
                Serialization.save( beanExplorerProject, beanExplorerProject.getLocation() );
            }
            catch ( ProjectSaveException e )
            {
                throw new BuildException( e );
            }
        }
    }

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
            throw new BuildException( e );
        }
    }

    private void checkDdl()
    {
        String entityName = getProject().getProperty( "BE4_CHECK_DDL" );
        if(entityName != null)
        {
            Entity entity = beanExplorerProject.getEntity( entityName );
            if(entity == null)
            {
                throw new BuildException( "Invalid entity: "+entityName );
            }
            DdlElement scheme = entity.getScheme();
            if(scheme == null)
            {
                throw new BuildException( "Entity has no scheme: "+entityName );
            }
            System.err.println( scheme.getDdl().replaceAll( "\n", System.lineSeparator() ) );
        }
    }

    private void checkRoles()
    {
        if(isProperty( "BE4_CHECK_ROLES" ) )
        {
            System.err.println( "Available roles:\n" + String.join( System.lineSeparator(), beanExplorerProject.getAvailableRoles() ) );
        }
    }

    private void checkQuery()
    {
        String path = getProject().getProperty( "BE4_CHECK_QUERY" );
        if(path == null)
            return;
        int pos = path.indexOf( '.' );
        if(pos <= 0)
        {
            throw new BuildException( "Invalid path supplied: "+path );
        }
        String entityName = path.substring( 0, pos );
        String queryName = path.substring( pos+1 );
        Entity entity = beanExplorerProject.getEntity( entityName );
        if(entity == null)
        {
            throw new BuildException("Invalid entity: "+entityName);
        }
        Query query = entity.getQueries().get( queryName );
        if(query == null)
        {
            try
            {
                queryName = new String(queryName.getBytes( "CP866" ), "CP1251");
                query = entity.getQueries().get( queryName );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new BuildException(e);
            }
        }
        if(query == null)
        {
            throw new BuildException("Invalid query: "+queryName);
        }
        System.err.println( query.getQueryCompiled().getResult().replaceAll( "\n", System.lineSeparator() ) );
    }

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
            catch ( BuildException e )
            {
                throw e;
            }
            catch ( RuntimeException e )
            {
                throw new BuildException( e );
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
        catch ( BuildException e )
        {
            throw e;
        }
        catch ( RuntimeException | IOException e )
        {
            throw new BuildException( e );
        }
    }

    protected void setAntProperties( BeElementWithProperties element )
    {
        for ( String property : element.getPropertyNames() )
        {
            setProperty( property, element.getProperty( property ) );
        }
    }
}
