package com.developmentontheedge.be5.metadata.scripts;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class AppDb extends ScriptSupport<AppDb>
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

    public void setModule(String module)
    {
        this.moduleName = module;
    }

    @Override
    public void execute() throws ScriptException
    {
        init();
        try
        {
            ps = createPrintStream((moduleName == null ? be5Project.getName() : moduleName) + "_db.sql");
            sql = new BeSqlExecutor(connector, ps);
            processProject();
            logger.info("Processed tables: " + createdTables + ", processed views: " + createdViews);
        }
        catch (ScriptException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ScriptException("Setup db error", e);
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
        logSqlFilePath();
    }

    protected void processProject()
    {
        processAllModules(module -> {
            BeVectorCollection<FreemarkerScript> scripts = module.getOrCreateCollection(Module.SCRIPTS, FreemarkerScript.class);
            sql.executeScript(scripts.get(FreemarkerCatalog.PRE_DB_STEP), logger);
        });
        processAllModules(module -> processDdlElements(module, TableDef.class, DdlElement::getCreateDdl));
        processAllModules(module -> processDdlElements(module, ViewDef.class, DdlElement::getCreateDdl));
        processAllModules(module -> {
            BeVectorCollection<FreemarkerScript> scripts = module.getOrCreateCollection(Module.SCRIPTS, FreemarkerScript.class);
            sql.executeScript(scripts.get(FreemarkerCatalog.POST_DB_STEP), logger);
        });
    }

    void processAllModules(Consumer<Module> processModule)
    {
        if (moduleName != null)
        {
            Module module = be5Project.getModule(moduleName);
            if (module == null)
            {
                throw new ScriptException("Module '" + moduleName + "' not found!");
            }
            processModule.accept(module);
        }
        else
        {
            for (Module module : be5Project.getModules())
            {
                if (ModuleLoader2.containsModule(module.getName(), logger))
                    processModule.accept(module);
            }
            processModule.accept(be5Project.getApplication());
        }
    }

    void processDdlElements(final Module module, Class<? extends DdlElement> ddlElementType,
                                      Function<DdlElement, String> getSqlFunction)
    {
        boolean started = false;
        List<Entity> entities = new ArrayList<>(module.getOrCreateEntityCollection().getAvailableElements());
        for (Entity entity : entities)
        {
            DdlElement scheme = entity.getScheme();
            if (scheme != null && scheme.getClass() == ddlElementType)
            {
                if (scheme.getClass() == TableDef.class)
                {
                    createdTables++;
                }
                else
                {
                    createdViews++;
                }

                if (scheme.withoutDbScheme())
                {
                    if (!started)
                    {
                        logger.setOperationName("[A] " + module.getCompletePath());
                        started = true;
                    }
                    executeMultiple(getSqlFunction.apply(scheme));
                }
                else
                {
                    logger.setOperationName("Skip table with schema: " + scheme.getEntityName());
                }
            }
        }
    }

    private void executeMultiple(String sqls)
    {
        try
        {
            sql.executeMultiple(sqls);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
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

    @Override
    public AppDb me()
    {
        return this;
    }
}
