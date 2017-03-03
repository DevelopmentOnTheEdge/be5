package com.developmentontheedge.be5.metadata.ant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import org.apache.tools.ant.BuildException;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entities;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityLocalizations;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.LanguageLocalizations;
import com.developmentontheedge.be5.metadata.model.LocalizationElement;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.ParseResult;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.SourceFile;
import com.developmentontheedge.be5.metadata.model.SourceFileOperation;
import com.developmentontheedge.be5.metadata.model.base.BeCaseInsensitiveCollection;
import com.developmentontheedge.be5.metadata.model.base.BeElementWithOriginModule;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;
import com.developmentontheedge.be5.metadata.sql.macro.IMacroProcessorStrategy;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

public class AppTools extends BETask
{
    @Override
    public void execute() throws BuildException
    {
        initParameters();
        String dumpTables = getProject().getProperty( "BE4_DUMP_TABLES" );
        if(dumpTables != null)
        {
            dumpTables( dumpTables );
        }
        if(isProperty( "BE4_SQL_CONSOLE" ))
        {
            sqlConsole();
        }
    }

    private void sqlConsole()
    {
        BeConnectionProfile prof = beanExplorerProject.getConnectionProfile();
        if(prof == null)
        {
            throw new BuildException( "Connection profile is required for SQL console" );
        }
        try
        {
            System.err.println("Welcome to FTL/SQL console!");
            BeSqlExecutor sql = new BeSqlExecutor( connector )
            {
                @Override
                public void executeSingle( String statement ) throws ExtendedSqlException
                {
                    ResultSet rs = null;
                    try
                    {
                        rs = connector.executeQuery( statement );
                        format(rs, System.err, 20);
                    }
                    catch ( SQLException e )
                    {
                        throw new ExtendedSqlException( connector, statement, e );
                    }
                    finally
                    {
                        connector.close( rs );
                    }
                }
            };
            FreemarkerScript fs = new FreemarkerScript( "SQL", beanExplorerProject.getApplication().getFreemarkerScripts() );
            DataElementUtils.save( fs );
            ProcessController log = new NullLogger();
            while(true)
            {
                System.err.println("Enter FTL/SQL (use 'quit' to exit):");
                String line = new BufferedReader( new InputStreamReader( System.in ) ).readLine();
                if(line == null)
                {
                    break;
                }
                line = line.trim();
                if(line.equals( "quit" ))
                {
                    break;
                }
                fs.setSource( line );
                ParseResult result = fs.getResult();
                if(result.getResult() != null) {
                    System.err.println("SQL> "+result.getResult());
                } else {
                    System.err.println("ERROR> "+result.getError());
                    continue;
                }
                try
                {
                    sql.executeScript( fs, log );
                }
                catch ( Exception e )
                {
                    System.err.println("ERROR> "+e.getMessage());
                }
            }
        }
        catch ( Exception e )
        {
            throw new BuildException(e);
        }
    }

    protected void format( ResultSet rs, PrintStream out, int limit ) throws SQLException
    {
        List<String> cols = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        for(int i=1; i<=count; i++) 
        {
            cols.add( metaData.getColumnLabel( i ) );
        }
        
        boolean ellipsis = false;
        while(rs.next())
        {
            if(rows.size() == limit) {
                ellipsis = true;
                break;
            }
            List<String> row = new ArrayList<>();
            for(int i=1; i<=count; i++)
            {
                String val = String.valueOf(rs.getString( i ));
                if(val.length() > 100)
                    val = val.substring( 0, 97 )+"...";
                row.add( val );
            }
            rows.add( row );
        }
        int[] lengths = IntStreamEx
                .ofIndices( cols )
                .map( col -> StreamEx.of( cols.get( col ) ).append( StreamEx.of( rows ).map( row -> row.get( col ) ) )
                        .mapToInt( String::length ).max().orElse( 1 ) ).toArray();
        out.println( IntStreamEx.ofIndices( cols ).mapToObj( col -> String.format( Locale.ENGLISH, "%-"+lengths[col]+"s", cols.get(col) ) )
                .joining( " | ", "| ", " |" ));
        out.println( IntStreamEx.of(lengths).mapToObj( len -> StreamEx.constant( "-", len ).joining() ).joining( "-|-", "|-", "-|" ));
        for(List<String> row : rows) {
            out.println( IntStreamEx.ofIndices( cols ).mapToObj( col -> String.format( Locale.ENGLISH, "%-"+lengths[col]+"s", row.get(col) ) )
                    .joining( " | ", "| ", " |" ));
        }
        if(ellipsis) {
            out.println("...");
        }
    }

    private void dumpTables( String dumpTables )
    {
        try
        {
            System.err.println("Reading db...");
            Project oldProject = new SqlModelReader( connector, SqlModelReader.READ_ALL | SqlModelReader.USE_HEURISTICS
                | SqlModelReader.ADVANCED_HEURISTICS ).readProject( beanExplorerProject.getName(), beanExplorerProject.isModuleProject() );
            System.err.println("Creating project...");
            createYaml(oldProject, dumpTables);
        }
        catch ( ExtendedSqlException | SQLException | ProcessInterruptedException e )
        {
            throw new BuildException( e );
        }
    }

    private void createYaml( Project prj , String entities )
    {
        Project project = new Project( "dump", true );
        Localizations newLocalizations = project.getApplication().getLocalizations();
        BeConnectionProfile connectionProfile = beanExplorerProject.getConnectionProfile();
        DataElementUtils.save( connectionProfile.clone( project.getConnectionProfiles().getLocalProfiles(),
                beanExplorerProject.getConnectionProfileName() ) );
        project.setConnectionProfileName( beanExplorerProject.getConnectionProfileName() );
        for ( String table : entities.split( "," ) )
        {
            table = table.trim();
            Entity entity = prj.getEntity( table );
            if ( entity == null )
            {
                throw new BuildException( "Entity '" + table + "' is not found" );
            }
            Entities entityCollection = project.getApplication().getOrCreateEntityCollection();
            Entity clone = entity.clone( entityCollection, entity.getName(), false );
            if(clone.getType() == EntityType.DICTIONARY)
            {
                addDictionaryDump(clone);
            }
            resetOrigin(project, clone);
            DataElementUtils.save( clone );
            for ( Module module : prj.getModulesAndApplication() )
            {
                for ( LanguageLocalizations langLoc : module.getLocalizations() )
                {
                    EntityLocalizations entityLoc = langLoc.get( table );
                    if ( entityLoc != null )
                    {
                        for ( LocalizationElement element : entityLoc.elements() )
                        {
                            newLocalizations.addLocalization( langLoc.getName(), table, element.getTopics(), element.getKey(),
                                    element.getValue() );
                        }
                    }
                }
            }
        }
        Path dumpPath = Paths.get( "yamlDump" );
        try
        {
            Serialization.save( project, dumpPath );
            System.err.println( "Entities are dumped as new project into " + dumpPath );
        }
        catch ( ProjectSaveException e )
        {
            throw new BuildException( e );
        }
    }

    private void addDictionaryDump( Entity entity )
    {
        ResultSet rs = null;
        Project project = entity.getProject();
        DatabaseConnector connector = DatabaseUtils.createConnector( project );
        FreemarkerCatalog scripts = project.getApplication().getFreemarkerScripts();
        FreemarkerCatalog dictionaries = ( FreemarkerCatalog ) scripts.get( "dict" );
        if(dictionaries == null)
        {
            dictionaries = new FreemarkerCatalog( "dict", scripts );
            DataElementUtils.save( dictionaries );
        }
        FreemarkerScript dict = ( FreemarkerScript ) scripts.get( "dictionaries" );
        if(dict == null)
        {
            dict = new FreemarkerScript( "dictionaries", scripts );
            DataElementUtils.save( dict );
            dict.setSource( "<#-- Dictionary dumps -->\n\n" );
        }
        try
        {
            BeSqlExecutor sql = new BeSqlExecutor( connector );
            rs = sql.executeNamedQuery( "sql.countRows", entity.getName() );
            if(!rs.next())
                throw new BuildException("Unexpected");
            int count = rs.getInt( 1 );
            if(count > 0 && count < 10000)
            {
                System.err.println("Dumping dictionary: "+entity.getName()+" ("+count+" entries)");
            }
            rs.close();
            rs = sql.executeNamedQuery( "sql.selectAll", entity.getName() );
            StringBuilder sb = new StringBuilder("<#-- Dictionary: "+entity.getName()+" -->\n\nDELETE FROM "+entity.getName()+";\n");
            BeCaseInsensitiveCollection<ColumnDef> columns = entity.findTableDefinition().getColumns();
            IMacroProcessorStrategy mps = project.getDatabaseSystem().getMacroProcessorStrategy();
            while(rs.next())
            {
                List<String> vals = new ArrayList<>();
                for(ColumnDef col : columns)
                {
                    int i = vals.size()+1;
                    String val;
                    if(col.getType().isIntegral())
                    {
                        val = String.valueOf( rs.getLong( i ) );
                    } else
                    {
                        val = mps.str( String.valueOf(rs.getString( i )) );
                    }
                    vals.add( rs.wasNull() ? "NULL" : val );
                }
                sb.append( "INSERT INTO " + entity.getName() + " VALUES(" + String.join( ", ", vals ) + ");\n" );
            }
            FreemarkerScript script = new FreemarkerScript( entity.getName(), dictionaries );
            script.setSource( sb.toString() );
            DataElementUtils.save( script );
            dict.setSource( dict.getSource()+"<#include 'dict/"+entity.getName()+"'/>\n" );
        }
        catch ( IOException | ExtendedSqlException | SQLException e )
        {
            throw new BuildException( e );
        }
        finally
        {
            connector.close( rs );
        }
    }

    private void resetOrigin( Project project, BeModelCollection<?> collection )
    {
        for(BeModelElement element : collection)
        {
            if(element instanceof SourceFileOperation)
            {
                SourceFile file = ((SourceFileOperation)element).getSourceFile();
                if(file != null && file.getOrigin() != null)
                {
                    project.getApplication().addSourceFile( file.getOrigin().getName(), file.getName(), file.getSource() );
                }
            }
            if(element instanceof BeElementWithOriginModule)
            {
                ((BeElementWithOriginModule)element).setOriginModuleName( project.getProjectOrigin() );
            }
            if(element instanceof BeModelCollection)
            {
                resetOrigin( project, ( BeModelCollection<?> ) element );
            }
        }
    }
}
