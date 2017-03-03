package com.developmentontheedge.be5.metadata;

import junit.framework.TestCase;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.TestDB;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;

/**
 * Reads metadata model from database (Biostore).
 */
public class ReadModelFromDatabaseTest extends TestCase
{
    public static String CONNECT_STRING = "jdbc:mysql://localhost:3306/biostore?user=biostore;password=biostore";
//    private static final String CONNECT_STRING = "jdbc:postgresql://localhost:5432/module?user=module;password=module";

    private DatabaseConnector connector;

    /** Standard JUnit constructor */
    public ReadModelFromDatabaseTest(String name)
    {
        super(name);
    }

    @ Override
    protected void setUp() throws Exception
    {
        super.setUp();
        connector = TestDB.getConnector(CONNECT_STRING);
    }

    @ Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        connector.closeConnection(null);
    }

    public void testReadModel() throws Exception
    {       
        assertNotNull(connector);
        System.out.println("connector=" + connector);
    }
  
    public void testSqlModelReader() throws Exception
    {
        Project project = new SqlModelReader(connector).readProject("TestProject");

        System.out.println(project.dump());
    }
    
    public void testReadRealtyModule() throws Exception
    {
        connector = TestDB.getConnector( CONNECT_STRING );
        Project project = new SqlModelReader( connector ).readProject( "realty", true );
        Serialization.save( project, ModuleUtils.getDefaultModulePath( "realty" ) );
    }
    
    public void testRealtyStructure() throws Exception
    {
        connector = TestDB.getConnector( CONNECT_STRING );
        Project project = new SqlModelReader( connector ).readProject( "realty", true );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        LoadContext context = new LoadContext();
        Project project2 = ModuleUtils.loadModule( "realty", context );
        project2.setDatabaseSystem( Rdbms.POSTGRESQL );
        assertTrue(context.getWarnings().isEmpty());
        for(Entity entity : project.getApplication().getEntities())
        {
            TableDef def = entity.findTableDefinition();
            TableDef def2 = project2.getApplication().getEntity( entity.getName() ).findTableDefinition();
            if ( def == null && def2 == null )
                continue;
            assertTrue( def != null && def2 != null );
            assertEquals(def.getCompletePath().toString(), def.getDdl(), def2.getDdl());
        }
    }
    
    public void testReadSecurityModule() throws Exception
    {
        connector = TestDB.getConnector( CONNECT_STRING );
        Project project = new SqlModelReader( connector ).readProject( "security", true );
        Serialization.save( project, ModuleUtils.getDefaultModulePath( "security" ) );
    }
    
    public void testReadFinancialModule() throws Exception
    {
        connector = TestDB.getConnector( CONNECT_STRING );
        Project project = new SqlModelReader( connector ).readProject( "financial", true );
        Serialization.save( project, ModuleUtils.getDefaultModulePath( "financial" ) );
    }
    
    public void testReadBeanExplorerModule() throws Exception
    {
        connector = TestDB.getConnector( CONNECT_STRING );
        Project project = new SqlModelReader( connector ).readProject( ModuleUtils.SYSTEM_MODULE, true );
        Serialization.save( project, ModuleUtils.getDefaultModulePath( "beanexplorer_draft" ) );
    }
} 
