package com.developmentontheedge.be5.metadata.scripts;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SimpleConnector;

import java.io.File;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public abstract class ScriptSupport<T>
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public abstract void execute() throws ScriptException;

    public abstract T me();

    public ProcessController logger = new JULLogger(log);

    public DbmsConnector connector;

    ///////////////////////////////////////////////////////////////////
    // Properties
    //
    public File projectPath;

    public boolean unlockProtectedProfile = false;

    public boolean debug = false;

    public File sqlPath = Paths.get("target/sql").toFile();

    public String connectionProfileName;

    public String connectionPassword;

    public Project be5Project;

    ///////////////////////////////////////////////////////////////////

    private File sqlFile;

    public void init()
    {
        initProject();
        initConnector();
    }

    private void initConnector()
    {
        if (connectionProfileName != null)
        {
            be5Project.setConnectionProfileName(connectionProfileName);
        }

        BeConnectionProfile profile = be5Project.getConnectionProfile();

        if (profile != null)
        {
            this.be5Project.setDatabaseSystem(Rdbms.getRdbms(profile.getConnectionUrl()));

            this.connector = new SimpleConnector(Rdbms.getRdbms(profile.getConnectionUrl()).getType(),
                    profile.getJdbcUrl().createConnectionUrl(false), profile.getUsername(),
                    connectionPassword != null ? connectionPassword : profile.getPassword());

            logger.info("Connection profile: '" + profile.getName() + "'" );
            logger.info("Using connection: " + DatabaseUtils.formatUrl(profile.getConnectionUrl(),
                    profile.getUsername(), "xxxxx"));
        }
        else
        {
            throw new ScriptException(
                    "Please specify connection profile: create "
                            + be5Project.getProjectFileStructure().getSelectedProfileFile()
                            + " file with profile name or use -DBE5_PROFILE=...");
        }
    }

    protected void initProject()
    {
        if (be5Project == null)
        {
            if (projectPath == null)
                throw new ScriptException("Please specify projectPath attribute");

            logger.info("Reading project from '" + projectPath + "'");

            try
            {
                be5Project = ModuleLoader2.loadProjectWithModules(projectPath.toPath(), logger);
            }
            catch (ProjectLoadException | MalformedURLException e)
            {
                e.printStackTrace();
            }

            if (debug)
            {
                be5Project.setDebugStream(System.err);
            }
        }
    }

    PrintStream createPrintStream(String name)
    {
        if (sqlPath != null)
        {
            sqlPath.mkdirs();
            try
            {
                sqlFile = new File(sqlPath, name);
                return new PrintStream(sqlFile, "UTF-8");
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    void logSqlFilePath()
    {
        if (sqlPath != null)
        {
            logger.info("Logs: " + sqlFile.getAbsolutePath());
        }
    }

//    public void checkErrors(final LoadContext loadContext, String messageTemplate) throws ScriptException
//    {
//        if (!loadContext.getWarnings().isEmpty())
//        {
//            for (ReadException exception : loadContext.getWarnings())
//            {
//                if (debug)
//                {
//                    exception.printStackTrace();
//                }
//                else
//                {
//                    logger.error("Error: " + exception.getMessage());
//                }
//            }
//            throw new ScriptException(messageTemplate.replace("%d",
// String.valueOf(loadContext.getWarnings().size())));
//        }
//    }

//    public void dumpSql( String ddlString )
//    {
//        logger.error( MultiSqlParser.normalize( be5Project.getDatabaseSystem().getType(), ddlString ) );
//    }
//
//    ///////////////////////////////////////////////////////////////////
//
//    public String readString( String prompt, String defaultValue, Object... values ) throws IOException
//    {
//        StringBuilder fullPrompt = new StringBuilder(prompt);
//        Set<String> vals = new TreeSet<>();
//        if(values != null)
//        {
//            for(Object value : values)
//                vals.add( value.toString().toLowerCase() );
//        }
//        if(!vals.isEmpty())
//        {
//            fullPrompt.append( " (" ).append( String.join( ", ", vals ) );
//            if(defaultValue != null)
//            {
//                fullPrompt.append( "; default: " ).append( defaultValue );
//            }
//            fullPrompt.append(")");
//        } else if(defaultValue != null)
//        {
//            fullPrompt.append( "(default: " ).append( defaultValue ).append( ")" );
//        }
//        fullPrompt.append( ": " );
//        String result;
//        do
//        {
//            logger.error( fullPrompt );
//            String line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
//            result = line == null ? "" : line.trim();
//            if(result.isEmpty() && defaultValue != null)
//                result = defaultValue;
//        } while(!vals.isEmpty() && !vals.contains( result ));
//        return result;
//    }

    public T setLogPath(File logPath)
    {
        this.sqlPath = logPath;
        return me();
    }

    public T setLogger(ProcessController logger)
    {
        this.logger = logger;
        return me();
    }

    public T setBe5Project(Project be5Project)
    {
        this.be5Project = be5Project;
        return me();
    }

    public T setBe5ProjectPath(String path)
    {
        projectPath = Paths.get(path).toFile();
        return me();
    }

    public T setBe5ProjectPath(Path path)
    {
        projectPath = path.toFile();
        return me();
    }

    public T setProfileName(String connectionProfileName)
    {
        this.connectionProfileName = connectionProfileName;
        return me();
    }

    public T setConnectionPassword(String connectionPassword)
    {
        this.connectionPassword = connectionPassword;
        return me();
    }

    public T setDebug(boolean debug)
    {
        this.debug = debug;
        return me();
    }
}
