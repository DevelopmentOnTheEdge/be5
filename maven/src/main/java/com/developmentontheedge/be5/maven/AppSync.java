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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

@Mojo( name = "sync")
public class AppSync extends Be5Mojo
{
    @Parameter (property = "BE5_UPDATE_CLONES")
    protected boolean updateClones;

    @Parameter (property = "BE5_FORCE_UPDATE")
    protected boolean forceUpdate;

    private File appPath;

    ///////////////////////////////////////////////////////////////////
    
    @Override
    public void execute() throws MojoFailureException
    {
        init();
        mergeModules();

        SyncMode syncMode = SyncMode.DDL;
        if( updateClones )
            syncMode = SyncMode.DDL_CLONES;
        
        PrintStream ps = null;
        try
        {
            if(logPath != null)
            {
                logPath.mkdirs();
                ps = new PrintStream(new File(logPath, beanExplorerProject.getName() + "_sync_ddl.sql"), "UTF-8");
            }
            
            final LoadContext loadContext = new LoadContext();
            BeSqlExecutor sqlExecutor = new BeSqlExecutor(connector, ps);

            /*
            if(beanExplorerProject.isModuleProject() && appPath != null)
            {
                Project hostProject = loadProject( appPath.toPath() );
                ModuleUtils.addModuleScripts( hostProject );
                beanExplorerProject.mergeHostProject( hostProject );
            }

            Project oldProject = new SqlModelReader(connector, SqlModelReader.READ_TABLEDEFS)
            						.readProject( beanExplorerProject.getName(), beanExplorerProject.isModuleProject() );
            Project origProject = useMeta ? ModuleUtils.loadMetaProject( loadContext ) : oldProject;
            
            if(mergeModules)
            {
                ModuleUtils.mergeAllModules( beanExplorerProject, origProject, logger, loadContext );
                checkErrors( loadContext, "Modules loading failed with %d error(s)" );
            } else
            {
                beanExplorerProject.merge( origProject );
            }*/

            // PENDING ? needed
            //beanExplorerProject.applyMassChanges( loadContext );
            
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

            Project oldProject = new SqlModelReader(connector, SqlModelReader.READ_TABLEDEFS)
					.readProject( beanExplorerProject.getName(), beanExplorerProject.isModuleProject() );

            DatabaseSynchronizer databaseSynchronizer = new DatabaseSynchronizer(logger, sqlExecutor, beanExplorerProject);
            String ddlString = databaseSynchronizer.getDdlStatements(oldProject, updateClones, false);
            ddlString = MultiSqlParser.normalize(beanExplorerProject.getDatabaseSystem().getType(), ddlString);

            if(ddlString.isEmpty())
            {
            	this.getLog().info("Database scheme is up-to-date");
            	return;
            } 
            
            if( forceUpdate )
            {
                databaseSynchronizer.sync( syncMode, oldProject );
            }
            else
            {
                    System.err.println("The following statements should be executed to update database scheme:");
                    System.err.println(ddlString);
            }
            
            checkSynchronizationStatus(databaseSynchronizer);
            logger.setOperationName( "Finished" );
        }
        catch( MojoFailureException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException | ExtendedSqlException | SQLException e ) //ReadException | ProjectLoadException | SQLException e )
        {
            if(debug)
                throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
            throw new MojoFailureException("Synchronisation error: " + e.getMessage());
        }
        catch( IOException | ProcessInterruptedException e )
        {
            throw new MojoFailureException("Synchronisation error: " + e.getMessage(), e);
        }
        catch( Throwable t )
        {
        	t.printStackTrace();
            throw new MojoFailureException("Synchronisation error: " + t.getMessage(), t);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
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
