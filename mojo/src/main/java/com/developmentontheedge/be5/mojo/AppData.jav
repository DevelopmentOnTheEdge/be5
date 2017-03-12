package com.developmentontheedge.be5.mojo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.beanexplorer.enterprise.metadata.exception.FreemarkerSqlException;
import com.beanexplorer.enterprise.metadata.exception.ProjectElementException;
import com.beanexplorer.enterprise.metadata.freemarker.FreemarkerSqlHandler;
import com.beanexplorer.enterprise.metadata.model.FreemarkerCatalog;
import com.beanexplorer.enterprise.metadata.model.FreemarkerScript;
import com.beanexplorer.enterprise.metadata.model.Module;
import com.beanexplorer.enterprise.metadata.model.base.DataElementPath;
import com.beanexplorer.enterprise.metadata.sql.BeSqlExecutor;
import com.beanexplorer.enterprise.metadata.sql.DatabaseUtils;
import com.beanexplorer.enterprise.metadata.util.ModuleUtils;
import com.developmentontheedge.dbms.SqlExecutor;

public class AppData extends BETask
{
    private String script = FreemarkerCatalog.DATA;
    
    private boolean ignoreMissing = false;

    @Override
    public void execute() throws BuildException
    {
        initParameters();

        PrintStream ps = null;
        try
        {
            if(logDir != null)
            {
                logDir.mkdirs();
                ps = new PrintStream( new File( logDir, beanExplorerProject.getName() + "_scripts_" + script.replace( ';', '_' ).replace( ':', '.' ) + ".sql" ), "UTF-8" );
            }
            ModuleUtils.addModuleScripts( beanExplorerProject );
            if(script.contains( ":" ))
                mergeModules();
            List<FreemarkerScript> scripts = new ArrayList<>();
            for(String scriptName : script.split(";"))
            {
                int pos = scriptName.indexOf( ':' );
                FreemarkerCatalog scriptsCatalog = beanExplorerProject.getApplication().getFreemarkerScripts();
                if(pos > 0)
                {
                    String moduleName = scriptName.substring( 0, pos );
                    scriptName = scriptName.substring( pos+1 );
                    if(moduleName.equals( "all" ))
                    {
                        for(Module module : beanExplorerProject.getModules())
                        {
                            scriptsCatalog = module.getFreemarkerScripts();
                            if(scriptsCatalog == null)
                                continue;
                            FreemarkerScript script = scriptsCatalog.optScript( scriptName );
                            if(script == null)
                                continue;
                            scripts.add( script );
                        }
                        FreemarkerScript script = beanExplorerProject.getApplication().getFreemarkerScripts().optScript( scriptName );
                        if(script != null)
                        {
                            scripts.add( script );
                        }
                        continue;
                    } else
                    {
                        Module module = beanExplorerProject.getModule( moduleName );
                        if(module == null)
                        {
                            if(ignoreMissing)
                            {
                                System.err.println( "Warning: module '"+moduleName+"' not found" );
                                continue;
                            }
                            else
                                throw new BuildException( "Module '"+moduleName+"' not found" );
                        }
                        scriptsCatalog = module.getFreemarkerScripts();
                    }
                }
                FreemarkerScript freemarkerScript = scriptsCatalog == null ? null : scriptsCatalog.optScript( scriptName );
                if(freemarkerScript == null)
                {
                    if(ignoreMissing)
                    {
                        System.err.println( "Warning: FTL script "+scriptName+" not found");
                        continue;
                    }
                    else
                        throw new BuildException("FTL script "+scriptName+" not found");
                }
                scripts.add( freemarkerScript );
            }
            SqlExecutor sqlExecutor = new BeSqlExecutor( connector, ps );
            for(FreemarkerScript freemarkerScript : scripts)
            {
                executeScript( sqlExecutor, freemarkerScript );
            }
            DatabaseUtils.clearAllCache( sqlExecutor );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( ProjectElementException | FreemarkerSqlException e )
        {
            if(debug)
                throw new BuildException(e);
            else
                throw new BuildException(e.getMessage());
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
    
    protected void executeScript( final SqlExecutor sqlExecutor, FreemarkerScript freemarkerScript ) throws ProjectElementException, IOException
    {
        String compiled = freemarkerScript.getResult().validate();
        if(logDir != null)
        {
            Files.write(
                    logDir.toPath().resolve(
                            beanExplorerProject.getName() + "_script_" + freemarkerScript.getModule().getName() + "_"
                                + freemarkerScript.getName() + ".compiled" ), compiled.getBytes( StandardCharsets.UTF_8 ) );
        }
        String sql = compiled.trim();
        if(sql.isEmpty())
            return;
        DataElementPath path = freemarkerScript.getCompletePath();
        if(debug)
            System.err.println( sql );
        sqlExecutor.comment( "Execute " + path );
        new FreemarkerSqlHandler(sqlExecutor, debug, logger).execute(freemarkerScript);
    }

    public String getScriptName()
    {
        return script;
    }

    public void setScriptName( String scriptName )
    {
        this.script = scriptName;
    }

    public boolean isIgnoreMissing()
    {
        return ignoreMissing;
    }

    public void setIgnoreMissing( boolean ignoreMissing )
    {
        this.ignoreMissing = ignoreMissing;
    }
}
