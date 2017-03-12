package com.developmentontheedge.be5.mojo;

import java.io.File;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;

import com.beanexplorer.enterprise.metadata.exception.FreemarkerSqlException;
import com.beanexplorer.enterprise.metadata.exception.ProjectElementException;
import com.beanexplorer.enterprise.metadata.model.DdlElement;
import com.beanexplorer.enterprise.metadata.model.Entity;
import com.beanexplorer.enterprise.metadata.model.FreemarkerCatalog;
import com.beanexplorer.enterprise.metadata.model.FreemarkerScript;
import com.beanexplorer.enterprise.metadata.model.Module;
import com.beanexplorer.enterprise.metadata.model.TableDef;
import com.beanexplorer.enterprise.metadata.model.ViewDef;
import com.beanexplorer.enterprise.metadata.model.base.BeVectorCollection;
import com.beanexplorer.enterprise.metadata.sql.BeSqlExecutor;
import com.beanexplorer.enterprise.metadata.util.ModuleUtils;

public class AppDb extends BETask
{
    private BeSqlExecutor sql;
    private PrintStream ps;
    private String moduleName;

    @Override
    public void execute() throws BuildException
    {
        initParameters();
        
        try
        {
            if(isModules())
            {
                mergeModules();
            }
            if(logDir != null)
            {
                logDir.mkdirs();
                ps = new PrintStream( new File( logDir, (moduleName == null ? beanExplorerProject.getName() : moduleName) + "_db.sql" ), "UTF-8" );
            }
            sql = new BeSqlExecutor( connector, ps );
            if(moduleName != null)
            {
                Module module = beanExplorerProject.getModule( moduleName );
                if(module == null)
                {
                    throw new BuildException("Module '"+moduleName+"' not found!");
                }
                createDb(module);
            }
            else
            {
                for(Module module : beanExplorerProject.getModules())
                {
                    if ( isBe4Module( module ) )
                        createDb( module );
                }
                createDb(beanExplorerProject.getApplication());
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            if(debug)
                throw new BuildException( e );
            throw new BuildException( e.getMessage() );
        }
        catch( Exception e )
        {
            throw new BuildException(e);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }

    private boolean isBe4Module( Module module )
    {
        return ( useMeta && module.getName().equals( ModuleUtils.SYSTEM_MODULE ) ) || ModuleUtils.isModuleExist( module.getName() );
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

    public String getModule()
    {
        return moduleName;
    }

    public void setModule( String module )
    {
        this.moduleName = module;
    }
}
