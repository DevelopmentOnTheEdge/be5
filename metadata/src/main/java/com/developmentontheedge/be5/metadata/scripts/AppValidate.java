package com.developmentontheedge.be5.metadata.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

/**
 * Usage example: 
 * mvn be5:validate -DBE5_DEBUG=true
 */
public class AppValidate extends ScriptSupport<AppValidate>
{
    String rdbmsName;

    boolean skipValidation = false;

    String queryPath;

    boolean checkRoles;

    String ddlPath;

    boolean saveProject;

    @Override
    public void execute() throws ScriptException
    {
        initLogging();
        
        getLog().info("Reading project from " + projectPath );
        this.be5Project = loadProject( projectPath.toPath() );

        setRdbms();
        loadModules();
        validateProject();
        checkQuery();
        checkRoles();
        checkDdl();
        saveProject();
        checkProfileProtection();
    }

    private void checkProfileProtection() throws ScriptException
    {
        if(be5Project.getConnectionProfile() != null &&
                be5Project.getConnectionProfile().isProtected() &&
                ! unlockProtectedProfile )
        {
            System.err.println( "=== WARNING! ===" ); 
            System.err.println( "You are using the protected profile '" + be5Project.getConnectionProfileName()+"'");
            System.err.println( "The following database may be modified due to this command: " + be5Project.getConnectionProfile().getConnectionUrl());
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
            if(be5Project.getConnectionProfileName().equals( line ))
            {
                unlockProtectedProfile = true;
            } else 
            {
                throw new ScriptException("Aborted");
            }
        }
    }

    private void loadModules() throws ScriptException
    {
        LoadContext loadContext = new LoadContext();
        //List<ProjectElementException> errors = new ArrayList<>();
        try
        {
//            final Project model = be5Project;
//            List<Project> moduleProjects = ModuleLoader2.loadModules(model, logger, loadContext);
//            errors.addAll( validateDeps(moduleProjects) );
//            ModuleLoader2.mergeAllModules( model, moduleProjects, loadContext );

            ModuleLoader2.mergeModules(be5Project, logger);
        }
        catch ( ProjectLoadException e )
        {
            throw new ScriptException("Can not load project modules", e);
        }
        checkErrors( loadContext, "Modules have %d error(s)" );
    }

    private void setRdbms()
    {
        // Need to set any system to validate project
        if(rdbmsName != null)
            be5Project.setDatabaseSystem( Rdbms.valueOf( rdbmsName.toUpperCase(Locale.ENGLISH) ) );
        if(be5Project.getDatabaseSystem() == null)
        {
            be5Project.setDatabaseSystem( Rdbms.POSTGRESQL );
        }
    }

    private void validateProject() throws ScriptException
    {
        List<ProjectElementException> errors = new ArrayList<>();
        if( skipValidation )
        {
            getLog().info("Validation skipped");
        } 
        else
        {
            getLog().info("Validating...");
            errors.addAll( be5Project.getErrors() );
            int count = 0;
            for(ProjectElementException error : errors)
            {
                if(error.getPath().equals( be5Project.getName() ) && error.getProperty().equals( "connectionProfileName" ))
                    continue;
                count++;
                displayError( error );
            }
            if(count > 0)
            {
                throw new ScriptException("Project has " + count + " errors." );
            }
            
            getLog().info("Project is valid.");
            skipValidation = true;
        }
    }

//    private List<ProjectElementException> validateDeps( List<Project> moduleProjects )
//    {
//        List<ProjectElementException> moduleErrors = new ArrayList<>();
//        Map<String, String> entityToModule = new HashMap<>();
//        for(Project prj : moduleProjects)
//        {
//            for(Entity entity : prj.getApplication().getEntities())
//                entityToModule.put( entity.getName(), prj.getName() );
//        }
//        for(Project prj : moduleProjects)
//        {
//            for(Entity entity : prj.getApplication().getEntities())
//            {
//                for(TableReference ref : entity.getAllReferences())
//                {
//                    String moduleTo = entityToModule.get( ref.getTableTo() );
//                    if(moduleTo != null && prj.getModule( moduleTo ) == null)
//                    {
//                        moduleErrors.add( new ProjectElementException( ref, "Reference to entity '" + ref.getTableTo()
//                            + "' which is defined in module '" + moduleTo + "' which is not specified in dependencies of module '"
//                            + prj.getName() + "'" ) );
//                    }
//                }
//            }
//        }
//        return moduleErrors;
//    }

    private void saveProject() throws ScriptException
    {
        if( saveProject )
        {
            try
            {
                getLog().info("Saving...");
                Serialization.save(be5Project, be5Project.getLocation());
            }
            catch(ProjectSaveException e)
            {
                throw new ScriptException("Can not save project.", e);
            }
        }
    }

    private void checkDdl() throws ScriptException
    {
        if( ddlPath != null)
        {
            Entity entity = be5Project.getEntity(ddlPath);
            if(entity == null)
            {
                throw new ScriptException("Invalid entity: " +  ddlPath);
            }

            DdlElement scheme = entity.getScheme();
            if(scheme == null)
            {
                throw new ScriptException("Entity has no scheme: " + ddlPath);
            }
            
            getLog().info("DDL: " + scheme.getDdl().replaceAll("\n", System.lineSeparator()));
        }
    }
    
    private void checkRoles()
    {
        if( checkRoles )
        {
            getLog().info("Available roles:" + System.lineSeparator() + " - " +
                    String.join( System.lineSeparator()+ " - " , be5Project.getAvailableRoles()));
        }
    }

    private void checkQuery() throws ScriptException
    {
        if( queryPath == null)
            return;
        
        int pos = queryPath.indexOf( '.' );
        if(pos <= 0)
        {
            throw new ScriptException("Invalid query path supplied: " + queryPath);
        }
        
        String entityName = queryPath.substring( 0, pos );
        String queryName  = queryPath.substring( pos+1 );
        Entity entity = be5Project.getEntity( entityName );
        if(entity == null)
        {
            throw new ScriptException("Invalid entity: " + entityName);
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
                throw new ScriptException("Can not load query, path=" + queryPath, e);
            }
        }

        if(query == null)
        {
            throw new ScriptException("Invalid query: "+queryName);
        }
        
        getLog().info("Query: " + query.getQueryCompiled().getResult().replaceAll( "\n", System.lineSeparator()) );
    }

    public AppValidate setCheckQueryPath(String queryPath)
    {
        this.queryPath = queryPath;
        return this;
    }

    public AppValidate setDdlPath(String ddlPath)
    {
        this.ddlPath = ddlPath;
        return this;
    }

    public AppValidate setCheckRoles(boolean checkRoles)
    {
        this.checkRoles = checkRoles;
        return this;
    }

    public AppValidate setRdbmsName(String rdbmsName)
    {
        this.rdbmsName = rdbmsName;
        return this;
    }

    public AppValidate setSkipValidation(boolean skipValidation)
    {
        this.skipValidation = skipValidation;
        return this;
    }

    public AppValidate setSaveProject(boolean saveProject)
    {
        this.saveProject = saveProject;
        return this;
    }

    @Override public AppValidate me() {
        return this;
    }
}
