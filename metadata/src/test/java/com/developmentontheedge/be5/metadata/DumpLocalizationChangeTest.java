package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import junit.framework.TestCase;

import com.beanexplorer.enterprise.client.testframework.TestDB;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.BeSqlExecutor;
import com.developmentontheedge.dbms.ExtendedSqlException;

public class DumpLocalizationChangeTest extends TestCase
{
    private static String CONNECT_STRING = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=lan3;user=sa;password=sa2008";
    private static String CONNECT_STRING_BE4 = "jdbc:sqlserver://winserv2012.dote.ru:1433;databaseName=lan4;user=sa;password=sa2008";
    private static String OUTPUT_DIR = "testmodule";
    
    public void testDumpLocalizationDiff() throws IOException, ExtendedSqlException, ProjectSaveException 
    {
        dumpLocalizationDiff(CONNECT_STRING, CONNECT_STRING_BE4, OUTPUT_DIR);
    }

    private void dumpLocalizationDiff( String db1, String db2, String path ) throws IOException, ExtendedSqlException, ProjectSaveException
    {
        BeSqlExecutor sql1 = new BeSqlExecutor( TestDB.getConnector(db1));
        BeSqlExecutor sql2 = new BeSqlExecutor( TestDB.getConnector(db2));
        Map<LocalizationEntry, String> l1 = getLocalizations( sql1 );
        Map<LocalizationEntry, String> l2 = getLocalizations( sql2 );
        Project prj = new Project( "testmodule", true );
        Map<LocalizationEntry, List<String>> added = new HashMap<>();
        for(Entry<LocalizationEntry, String> entry : l1.entrySet())
        {
            if(!entry.getValue().equals( l2.get( entry.getKey() ) ))
            {
                LocalizationEntry newEntry = new LocalizationEntry( entry.getKey().lang, entry.getKey().entity, entry.getValue(), entry.getKey().key );
                List<String> list = added.get( newEntry );
                if(list == null)
                {
                    list = new ArrayList<>();
                    added.put( newEntry, list );
                }
                list.add( entry.getKey().topicOrValue );
            }
        }
        for(Entry<LocalizationEntry, List<String>> entry : added.entrySet())
        {
            prj.getApplication().getLocalizations().addLocalization( entry.getKey().lang, entry.getKey().entity, entry.getValue(), entry.getKey().key, entry.getKey().topicOrValue );
        }
        Serialization.save( prj, Paths.get( path ));
    }
    
    private Map<LocalizationEntry, String> getLocalizations(BeSqlExecutor sql) throws ExtendedSqlException
    {
        ResultSet rs = sql.executeNamedQuery( "selectAllLocalizations" );
        Map<LocalizationEntry, String> localizations = new HashMap<>();
        try
        {
            while(rs.next())
            {
                localizations.put( new LocalizationEntry( rs.getString( 2 ), rs.getString(3), rs.getString(6), rs.getString(4) ), rs.getString(5) );
            }
            return localizations;
        }
        catch ( SQLException e )
        {
            throw new ExtendedSqlException( "", "", e );
        }
        finally
        {
            sql.close( rs );
        }
    }

    private static class LocalizationEntry
    {
        final String lang, entity, topicOrValue, key;

        public LocalizationEntry( String lang, String entity, String topicOrValue, String key )
        {
            this.lang = lang;
            this.entity = entity;
            this.topicOrValue = topicOrValue;
            this.key = key;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( entity, key, lang, topicOrValue );
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
                return true;
            if ( obj == null || getClass() != obj.getClass() )
                return false;
            LocalizationEntry other = ( LocalizationEntry ) obj;
            return Objects.equals( entity, other.entity ) &&
                    Objects.equals( key, other.key ) &&
                    Objects.equals( lang, other.lang ) &&
                    Objects.equals( topicOrValue, other.topicOrValue );
        }
    }
}
