package com.developmentontheedge.be5.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.PrintStream;

import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.exception.FreemarkerSqlException;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.ViewDef;
import com.developmentontheedge.be5.metadata.model.base.BeVectorCollection;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;

@Mojo( name = "create-db")
public class AppDb extends Be5Mojo
{
    private BeSqlExecutor sql;
    private PrintStream ps;

    private String moduleName;
    public String getModule()
    {
        return moduleName;
    }
    public void setModule( String module )
    {
        this.moduleName = module;
    }

    @Override
    public void execute() throws MojoFailureException
    {
        init();
        
        try
        {
            if(true) // TODOisModules())
            {
                mergeModules();
            }
            if( logPath != null)
            {
                logPath.mkdirs();
                ps = new PrintStream( new File(logPath, (moduleName == null ? beanExplorerProject.getName() : moduleName) + "_db.sql" ), "UTF-8" );
            }

            sql = new BeSqlExecutor(connector, ps); // TODO - properties - null, what should be?
            
            if( moduleName != null )
            {
                Module module = beanExplorerProject.getModule( moduleName );
                if(module == null)
                {
                    throw new MojoFailureException("Module '" + moduleName + "' not found!");
                }
                createDb(module);
            }
            else
            {
                for(Module module : beanExplorerProject.getModules())
                {
                    if( ModuleLoader2.containsModule(module.getName()) ) 
                        createDb( module );
                }
                createDb(beanExplorerProject.getApplication());
            }
        }
        catch( MojoFailureException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            if(debug)
                throw new MojoFailureException("Setup db error", e);
            
            throw new MojoFailureException(e.getMessage());
        }
        catch(Exception e)
        {
            throw new MojoFailureException("Setup db error", e);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }

    private void createDb( Module module ) throws ProjectElementException
    {
        BeVectorCollection<FreemarkerScript> scripts = module.getOrCreateCollection( Module.SCRIPTS, FreemarkerScript.class );
        sql.executeScript( scripts.get( FreemarkerCatalog.PRE_DB_STEP ), logger );
        execute(module);
        sql.executeScript( scripts.get( FreemarkerCatalog.POST_DB_STEP ), logger );
    }

    private void execute( final Module module ) throws ProjectElementException
    {
        boolean started = false;
        for(Entity entity : module.getOrCreateEntityCollection().getAvailableElements())
        {
            DdlElement scheme = entity.getScheme();
            if(scheme instanceof TableDef)
            {
                if(scheme.withoutDbScheme())
                {
                    if (!started) {
                        logger.setOperationName("[A] " + module.getCompletePath());
                        started = true;
                    }
                    processDdl(scheme);
                }
                else
                {
                    logger.setOperationName("Skip table with schema: " + scheme.getEntityName());
                }
            }
        }
        // Define views after tables as there might be dependencies
        for(Entity entity : module.getOrCreateEntityCollection().getAvailableElements())
        {
            DdlElement scheme = entity.getScheme();
            if(scheme instanceof ViewDef)
            {
                if(scheme.withoutDbScheme())
                {
                    if(!started)
                    {
                        logger.setOperationName( "[A] " + module.getCompletePath() );
                        started = true;
                    }
                    processDdl(scheme);
                }
                else
                {
                    logger.setOperationName("Skip table with schema: " + scheme.getEntityName());
                }
            }
        }
    }

    private void processDdl(final DdlElement tableDef) throws ProjectElementException
    {
        try
        {
            final String generatedQuery = tableDef.getDdl();
            sql.executeMultiple( generatedQuery );
        }
        catch( Exception e )
        {
            throw new ProjectElementException(tableDef, e);
        }
    }

}
