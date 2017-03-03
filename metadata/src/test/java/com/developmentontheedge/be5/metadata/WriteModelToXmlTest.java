package com.developmentontheedge.be5.metadata;

import java.nio.file.Files;
import java.nio.file.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.beanexplorer.enterprise.DatabaseConnector;
import com.beanexplorer.enterprise.client.testframework.TestDB;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.serialization.ProjectFileSystem;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.SqlModelReader;

/**
 * Writes metadata model to XML files.
 */
public final class WriteModelToXmlTest extends TestCase
{
//    public static String CONNECT_STRING = "jdbc:mysql://localhost:3306/biostore?user=biostore;password=biostore";
    private static final String CONNECT_STRING = "jdbc:postgresql://localhost:5432/condo_be4?user=condo;password=condo";

    private static DatabaseConnector connector;
    private static Project project;

    /** Standard JUnit constructor */
    public WriteModelToXmlTest(final String name)
    {
        super(name);
    }

    /** Make suite if tests. */
    public static Test suite ( )
    {
        final TestSuite suite = new TestSuite ( WriteModelToXmlTest.class.getName ( ) );
        
        suite.addTest ( new WriteModelToXmlTest ( "testReadSqlProject" ) );
        suite.addTest ( new WriteModelToXmlTest ( "testWriteXmlProject" ) );
        
        return suite;
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

    public void testReadSqlProject() throws Exception
    {
        project = new SqlModelReader(connector).readProject("TestProject");

//        System.out.println(project.dump());
    }

    public void testWriteXmlProject() throws Exception
    {
        project = new Project("TestProject");
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );
        
        final ProjectFileSystem fileSystem = new ProjectFileSystem( project );
        
        assertTrue( Files.isDirectory( tempFolder ) );
        assertTrue( Files.isRegularFile( fileSystem.getProjectFile() ) );
        TestUtils.assertFileEquals("data/simpleproject.xml", getClass().getResourceAsStream("data/simpleproject.xml"), Files.newInputStream(fileSystem.getProjectFile()));
        
        final Module module = project.getApplication();
        final Entity table = new Entity("testTable", module, EntityType.TABLE);
        DataElementUtils.saveQuiet(table);
        table.setDisplayName( "$project.Name/$module.Name/$entity.Name" );
        final Query query = new Query("All records", table);
        DataElementUtils.saveQuiet(query);
        query.setQuery("<@distinct \"name\"/>");
        project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY )
            .setSource( "<#macro distinct column>SELECT DISTINCT ${column} FROM ${entity.getName()}</#macro>" );
        
        final BeModelCollection<TableRef> tableRefs = table.getOrCreateTableReferences();
        final TableRef tableRef = new TableRef("ref1", "user", tableRefs);
        tableRef.setTableTo("users");
        tableRef.setColumnsTo("userName");
        DataElementUtils.saveQuiet(tableRef);
        
        Serialization.save( project, tempFolder );

        assertTrue( Files.isDirectory( fileSystem.getEntitiesFolder() ) );
        TestUtils.assertFileEquals("data/simpleproject2.xml", getClass().getResourceAsStream("data/simpleproject2.xml"), Files.newInputStream(fileSystem.getProjectFile()));
        
        FileUtils.deleteRecursively( tempFolder );
    }

} 
