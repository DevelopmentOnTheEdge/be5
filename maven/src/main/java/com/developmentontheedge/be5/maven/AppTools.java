package com.developmentontheedge.be5.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.ParseResult;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.ExtendedSqlException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "tools")
public class AppTools extends Be5Mojo
{

    @Override
    public void execute() throws MojoFailureException
    {
        init();
        mergeModules();

        BeConnectionProfile prof = be5Project.getConnectionProfile();
        if(prof == null)
        {
            throw new MojoFailureException("Connection profile is required for SQL console");
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
            FreemarkerScript fs = new FreemarkerScript( "SQL", be5Project.getApplication().getFreemarkerScripts() );
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
        catch (Exception e)
        {
            throw new MojoFailureException("Console error: " + e.getMessage(), e);
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

}
