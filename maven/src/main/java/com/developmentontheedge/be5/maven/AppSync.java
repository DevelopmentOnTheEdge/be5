package com.developmentontheedge.be5.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import com.developmentontheedge.be5.metadata.exception.FreemarkerSqlException;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlSerializer;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.sql.DatabaseSynchronizer;
import com.developmentontheedge.be5.metadata.sql.DatabaseSynchronizer.SyncMode;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.MultiSqlParser;

import one.util.streamex.StreamEx;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

@Mojo( name = "sync")
public class AppSync extends Be5Mojo
{
    @Parameter (property = "BE5_FORCE_UPDATE")
    protected boolean forceUpdate;

    @Parameter (property = "BE5_UPDATE_CLONES")
    protected boolean updateClones;

    private String mode = "all";
    public String getMode()             { return mode;  }
    public void setMode(String mode)    { this.mode = mode; }

    private boolean demo;
    public boolean isDemo()             { return demo; }
    public void setDemo(boolean demo)   { this.demo = demo;}

    private boolean mergeModules;
    public boolean isMergeModules()     { return mergeModules;}
    public void setMergeModules(boolean mergeModules)   { this.mergeModules = mergeModules; }

    private File appPath;
    public File getAppPath()            { return appPath;  }
    public void setAppPath(File appPath){ this.appPath = appPath; }

    ///////////////////////////////////////////////////////////////////
    
    @Override
    public void execute() throws MojoFailureException
    {
        initParameters();
        mergeModules |= modules;

        SyncMode syncMode = getSyncMode();
        PrintStream ps = null;
        try
        {
            final LoadContext loadContext = new LoadContext();
            if(logPath != null)
            {
                logPath.mkdirs();
                ps = new PrintStream( new File( logPath, beanExplorerProject.getName() + "_sync_" + mode + ".sql" ), "UTF-8" );
            }
            BeSqlExecutor sql = new BeSqlExecutor( connector, ps );
            if(!mergeModules)
            {
                ModuleUtils.addModuleScripts( beanExplorerProject );
            }
            if(beanExplorerProject.isModuleProject() && appPath != null)
            {
                Project hostProject = loadProject( appPath.toPath() );
                ModuleUtils.addModuleScripts( hostProject );
                beanExplorerProject.mergeHostProject( hostProject );

                /* TODO
                if(beanExplorerProject.getConnectionProfile() == null)
                {
                    String profileName = getProject().getProperty( "BE4_PROFILE" );
                    if(profileName != null)
                    {
                        hostProject.setConnectionProfileName( profileName );
                        BeConnectionProfile profile = hostProject.getConnectionProfile();
                        if(profile == null)
                        {
                            throw new MojoFailureException( "Cannot find connection profile '"+profileName+"'" );
                        }
                        DataElementUtils.save( profile.clone( beanExplorerProject.getConnectionProfiles().getLocalProfiles(), profile.getName() ) );
                        beanExplorerProject.setConnectionProfileName( profileName );
                    }
                }*/
            }
            Project oldProject = new SqlModelReader( connector, getReadMode( syncMode ) ).readProject( beanExplorerProject.getName(), beanExplorerProject.isModuleProject() );
            Project origProject = useMeta ? ModuleUtils.loadMetaProject( loadContext ) : oldProject;
            
            // Remove orphans temporary to exclude them from merging, massChanges processing, etc.
            /* TODO
            Module orphans = oldProject.getModules().get( SqlModelReader.ORPHANS_MODULE_NAME );
            oldProject.getModules().remove( SqlModelReader.ORPHANS_MODULE_NAME ); */
            
            if(mergeModules)
            {
                ModuleUtils.mergeAllModules( beanExplorerProject, origProject, logger, loadContext );
                checkErrors( loadContext, "Modules loading failed with %d error(s)" );
            } else
            {
                beanExplorerProject.merge( origProject );
            }

            beanExplorerProject.applyMassChanges( loadContext );
            
            if(beanExplorerProject.getDebugStream() != null)
            {
                LinkedHashMap<String, Object> serializedProfiles = new LinkedHashMap<>();
                serializedProfiles.put( beanExplorerProject.getConnectionProfileName(), YamlSerializer.serializeProfile( beanExplorerProject.getConnectionProfile() ) );
                String serialized = new Yaml().dump( serializedProfiles );
                beanExplorerProject.getDebugStream().println("Computed profile: "+serialized.replace( "\n", System.lineSeparator() ));
                beanExplorerProject.getDebugStream().println("Modules and extras for "+beanExplorerProject.getName()+":");
                beanExplorerProject.allModules()
                        .map( m -> "- " + m.getName() + ": " + ( m.getExtras() == null ? "" : String.join( ", ", m.getExtras() ) ) )
                        .forEach( beanExplorerProject.getDebugStream()::println );
                
                /* TODO
                if(orphans != null)
                {
                    beanExplorerProject.getDebugStream().println("Orphan tables: "+orphans.getEntityNames());
                } */
            }
            
            checkErrors( loadContext, "Mass changes failed with %d error(s)" );
            
            /* TODO
            // Put orphans back: DatabaseSynchronizer will use it
            if(orphans != null)
                DataElementUtils.save( orphans ); */
            
            DatabaseSynchronizer databaseSynchronizer = new DatabaseSynchronizer(logger, sql, beanExplorerProject);
            if((syncMode == SyncMode.DDL || syncMode == SyncMode.DDL_CLONES) && demo)
            {
                processDdlDemo( oldProject, syncMode == SyncMode.DDL_CLONES, databaseSynchronizer );
                return;
            }
            if((syncMode == SyncMode.ALL || syncMode == SyncMode.DDL || syncMode == SyncMode.DDL_CLONES) && !forceUpdate )
            {
                String ddlStatements = MultiSqlParser.normalize( beanExplorerProject.getDatabaseSystem().getType(),
                        databaseSynchronizer.getDdlStatements( oldProject, syncMode == SyncMode.DDL_CLONES, true ) );

                if(!ddlStatements.isEmpty())
                {
                    throw new MojoFailureException("Schema changes must be applied which may result in data loss or adding arbitrary default values!\n"
                        + "Please rerun with -DBE4_FORCE_UPDATE=true to update anyways.\n"
                        + "The following SQL statements are considered as dangerous:\n"
                        + ddlStatements);
                }
            }
            
            databaseSynchronizer.sync( syncMode, oldProject );
            checkSynchronizationStatus( databaseSynchronizer );
            logger.setOperationName( "Finished" );
        }
        catch( MojoFailureException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException | ExtendedSqlException | ReadException | ProjectLoadException | SQLException e )
        {
            if(debug)
                throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
            throw new MojoFailureException("Synchronisation error: " + e.getMessage());
        }
        catch( IOException | ProcessInterruptedException e )
        {
            throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }

    private SyncMode getSyncMode() throws MojoFailureException
    {
        SyncMode syncMode;
        try
        {
            syncMode = SyncMode.valueOf( mode.toUpperCase() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoFailureException( "Invalid mode specified (" + mode + "). Available modes are: "
                + StreamEx.of(SyncMode.values()).joining( ", " ) );
        }
        if ( syncMode == SyncMode.DDL && updateClones )
            syncMode = SyncMode.DDL_CLONES;
        return syncMode;
    }

    private int getReadMode( SyncMode syncMode )
    {
        switch(syncMode)
        {
        case DDL:
        case DDL_CLONES:
            return SqlModelReader.READ_TABLEDEFS; // TODO | SqlModelReader.READ_ORPHANS_MODULE;
        default:
            return SqlModelReader.READ_ALL;
        }
    }

    private void processDdlDemo( Project oldProject, boolean supportClones, DatabaseSynchronizer databaseSynchronizer ) throws ExtendedSqlException
    {
        String ddlString = databaseSynchronizer.getDdlStatements( oldProject, supportClones, false );
        if(ddlString.isEmpty())
        {
            System.err.println("Database scheme is up-to-date");
        } else
        {
            System.err.println("The following statements should be executed to update database scheme:");
            dumpSql( ddlString );
            checkSynchronizationStatus( databaseSynchronizer );
        }
    }

    protected void checkSynchronizationStatus( DatabaseSynchronizer databaseSynchronizer )
    {
        List<ProjectElementException> warnings = databaseSynchronizer.getWarnings();
        if(!warnings.isEmpty())
        {
            System.err.println( "Synchronization of "+databaseSynchronizer.getProject().getName()+" produced "+warnings.size()+" warning(s):" );
            for(ProjectElementException warning : warnings)
            {
                displayError( warning );
            }
        }
    }

}
