package com.developmentontheedge.be5.metadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.DatabaseUtils;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;
import com.developmentontheedge.be5.metadata.util.ProcessController;
import com.developmentontheedge.dbms.DbmsType;
import com.developmentontheedge.dbms.ExtendedSqlException;
import com.developmentontheedge.dbms.MultiSqlParser;

public class MiscTest extends TestCase
{
    public void testNormalizeSql() throws IOException
    {
        byte[] bytes = Files.readAllBytes( Paths.get( "C:\\projects\\java\\tik-region\\src\\sql\\oracle\\gen\\BMOD_realty_src_sql_attributes10.sql" ) );
        System.out.println(MultiSqlParser.normalize( DbmsType.ORACLE, new String(bytes, "KOI8-R") ));
    }
    
    public void testRegexp()
    {
        String code = " sadfsadf {\n   sdgsfdgsg    \r\n      sdfgsdfgsdfg\nfsgsdfg\n";
        System.out.println(Pattern.compile( "^\\s*(.*?)\\s*$", Pattern.MULTILINE ).matcher( code ).replaceAll( "$1" ));
    }
    
    public void testProjectMemoryFootprint() throws ProjectLoadException, ExtendedSqlException, SQLException, ProcessInterruptedException, InterruptedException
    {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long usedAtStart = runtime.totalMemory()-runtime.freeMemory();
        System.out.println("Used at start: "+usedAtStart);
        String projectPath = "C:\\projects\\java\\condo4";
        LoadContext loadContext = new LoadContext();
        Project project = Serialization.load( Paths.get( projectPath ), loadContext );
        loadContext.check();
        DatabaseConnector connector = DatabaseUtils.createConnector( project );
        ModuleUtils.mergeAllModules( project, new SqlModelReader( connector ).readProject( project.getName() ), (ProcessController)null, loadContext );
        loadContext.check();
        project.applyMassChanges( loadContext );
        loadContext.check();
        System.gc();
        long usedAtEnd = runtime.totalMemory()-runtime.freeMemory();
        System.out.println("Used at end: "+usedAtEnd);
        System.out.println("Delta: "+(usedAtEnd-usedAtStart));
        Thread.sleep( 100000 );
    }
}
