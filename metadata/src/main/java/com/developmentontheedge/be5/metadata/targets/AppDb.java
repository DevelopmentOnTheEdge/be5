package com.developmentontheedge.be5.metadata.targets;

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
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;

import java.io.PrintStream;


public class AppDb extends DatabaseOperationSupport<AppDb>
{
    private BeSqlExecutor sql;
    private PrintStream ps;

    private String moduleName;
    private int createdTables = 0;
    private int createdViews = 0;

    public String getModule()
    {
        return moduleName;
    }
    public void setModule( String module )
    {
        this.moduleName = module;
    }

    @Override
    public void execute() throws DatabaseTargetException
    {
        init();
        
        try
        {
            ps = createPrintStream((moduleName == null ? be5Project.getName() : moduleName) + "_db.sql");

            sql = new BeSqlExecutor(connector, ps);
            
            if( moduleName != null )
            {
                Module module = be5Project.getModule( moduleName );
                if(module == null)
                {
                    throw new DatabaseTargetException("Module '" + moduleName + "' not found!");
                }
                createDb(module);
            }
            else
            {
                for(Module module : be5Project.getModules())
                {
                    if( ModuleLoader2.containsModule(module.getName()) ) 
                        createDb( module );
                }
                createDb(be5Project.getApplication());
            }
            getLog().info("Created tables: " + createdTables + ", created views: " + createdViews);
        }
        catch( DatabaseTargetException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            if(debug) {
                e.printStackTrace();
                throw new DatabaseTargetException("Setup db error", e);
            }
            
            throw new DatabaseTargetException(e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new DatabaseTargetException("Setup db error", e);
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }

        logSqlFilePath();
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
                    createdTables++;
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
                    createdViews++;
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

    public int getCreatedTables()
    {
        return createdTables;
    }

    public int getCreatedViews()
    {
        return createdViews;
    }

    @Override public AppDb me() {
        return this;
    }
}
