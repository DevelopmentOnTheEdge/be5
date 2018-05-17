package com.developmentontheedge.be5.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

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
public class AppDb extends Be5Mojo<AppDb>
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
    public void execute() throws MojoFailureException
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
                    throw new MojoFailureException("Module '" + moduleName + "' not found!");
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
        catch( MojoFailureException e )
        {
            throw e;
        }
        catch ( ProjectElementException | FreemarkerSqlException e )
        {
            if(debug) {
                e.printStackTrace();
                throw new MojoFailureException("Setup db error", e);
            }
            
            throw new MojoFailureException(e.getMessage());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new MojoFailureException("Setup db error", e);
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

    @Override protected AppDb me() {
        return this;
    }
}
