package com.developmentontheedge.be5.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ReadException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.operations.DatabaseTargetException;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsConnector;
import com.developmentontheedge.dbms.SimpleConnector;

public abstract class DatabaseOperationSupport<T>
{
    private static final Logger log = Logger.getLogger(DatabaseOperationSupport.class.getName());

    public abstract void execute() throws DatabaseTargetException;

    protected abstract T me();

    protected ProcessController logger = new JULLogger(log);

	protected DbmsConnector connector;

    ///////////////////////////////////////////////////////////////////
    // Properties
    //
    public File projectPath;

    boolean unlockProtectedProfile = false;

    boolean debug = false;

    File logPath = Paths.get("target/sql").toFile();

    protected String connectionProfileName;

    protected String connectionPassword;

    protected Project be5Project;

    ///////////////////////////////////////////////////////////////////

    private File logFile;

    protected Logger getLog()
    {
        return log;
    }

    public void init() throws DatabaseTargetException
    {
        initProject();
        initConnector();
    }

    public void initConnector() throws DatabaseTargetException
    {
        if(connectionProfileName != null)
        {
            be5Project.setConnectionProfileName(connectionProfileName);
        }

        BeConnectionProfile profile = be5Project.getConnectionProfile();

        if (profile != null)
        {
            this.be5Project.setDatabaseSystem(Rdbms.getRdbms(profile.getConnectionUrl()));

            this.connector = new SimpleConnector(Rdbms.getRdbms(profile.getConnectionUrl()).getType(),
                    profile.getConnectionUrl(), profile.getUsername(),
                    connectionPassword != null ? connectionPassword : profile.getPassword());

            getLog().info("Using connection " + DatabaseUtils.formatUrl(profile.getConnectionUrl(), profile.getUsername(), "xxxxx"));
        }
        else
        {
            throw new DatabaseTargetException(
                    "Please specify connection profile: create "
                            + be5Project.getProjectFileStructure().getSelectedProfileFile()
                            + " file with profile name or use -DBE5_PROFILE=...");
        }
    }

    public void initProject() throws DatabaseTargetException
    {
        long startTime = System.nanoTime();
        initLogging();

        if(be5Project == null)
        {
            if (projectPath == null)
                throw new DatabaseTargetException("Please specify projectPath attribute");

            getLog().info("Reading be5 project from '" + projectPath + "'...");

            be5Project = loadProject(projectPath.toPath());
            if (debug)
            {
                be5Project.setDebugStream(System.err);
            }

            try
            {
                ModuleLoader2.mergeModules(be5Project, logger);
            }
            catch (ProjectLoadException e)
            {
                e.printStackTrace();
                throw new DatabaseTargetException(e.getMessage());
            }
        }

        getLog().info(ModuleLoader2.logLoadedProject(be5Project, startTime));
    }
    /**
     * Configures JUL (java.util.logging).
     */
    protected void initLogging()
    {
    	// configure JUL logging
    	String ln = System.lineSeparator();
    	String level = debug ? "FINEST" : "INFO";
    	String logConfig = 
//    			".level=" + level +
    			"handlers= java.util.logging.ConsoleHandler" + ln +
    			"java.util.logging.ConsoleHandler.level = " + level + ln +
     			"java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter" + ln +
    	        "java.util.logging.SimpleFormatter.format =%1$TT %4$s: %5$s%n";

    	// JUL - String.format(format, date, source, logger, level, message, thrown);
    	//                             1     2       3       4      5        6    
    	
    	try 
    	{
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(logConfig.getBytes(StandardCharsets.UTF_8)));
        } 
    	catch (IOException e) 
    	{
            System.err.println("Could not setup logger configuration: " + e.toString());
        }    	
    }

    protected Project loadProject(final Path root) throws DatabaseTargetException
    {
        final LoadContext loadContext = new LoadContext();
        Project prj;
        try
        {
            prj = Serialization.load(root, loadContext);
        }
        catch(ProjectLoadException | RuntimeException e)
        {
            throw new DatabaseTargetException("\nCan not load project", e);
        }
        checkErrors( loadContext, "Project has %d error(s)" );
        return prj;
    }

    PrintStream createPrintStream(String name)
    {
        if(logPath != null)
        {
            logPath.mkdirs();
            try
            {
                logFile = new File(logPath, name);
                return new PrintStream(logFile, "UTF-8");
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
        if(logPath != null)
        {
            getLog().info("Logs: " + logFile.getAbsolutePath());
        }
    }

    protected void displayError(ProjectElementException error)
    {
        error.format( System.err );
    }

    protected void checkErrors(final LoadContext loadContext, String messageTemplate) throws DatabaseTargetException
    {
        if(!loadContext.getWarnings().isEmpty())
        {
            for(ReadException exception : loadContext.getWarnings())
            {
                if(debug)
                {
                    exception.printStackTrace();
                } else
                {
                    System.err.println( "Error: "+exception.getMessage() );
                }
            }
            throw new DatabaseTargetException( messageTemplate.replace( "%d", String.valueOf( loadContext.getWarnings().size() ) ) );
        }
    }
    

//    protected void dumpSql( String ddlString )
//    {
//        System.err.println( MultiSqlParser.normalize( be5Project.getDatabaseSystem().getType(), ddlString ) );
//    }
//
//    ///////////////////////////////////////////////////////////////////
//
//    protected String readString( String prompt, String defaultValue, Object... values ) throws IOException
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
//            System.err.println( fullPrompt );
//            String line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
//            result = line == null ? "" : line.trim();
//            if(result.isEmpty() && defaultValue != null)
//                result = defaultValue;
//        } while(!vals.isEmpty() && !vals.contains( result ));
//        return result;
//    }

    public T setLogPath(File logPath)
    {
        this.logPath = logPath;
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
}
