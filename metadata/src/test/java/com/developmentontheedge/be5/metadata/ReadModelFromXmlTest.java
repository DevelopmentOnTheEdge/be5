package com.developmentontheedge.be5.metadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.JavaScriptOperationExtender;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlDeserializer;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlSerializer;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * Reads metadata model from XML files.
 */
public class ReadModelFromXmlTest
{
    @Test
    public void testWriteReadCompareXmlProject() throws Exception
    {
        final Project project = new Project("TestProject");
        project.setRoles( Arrays.asList( "Admin", "Guest" ) );
        final Module module = project.getApplication();
        final Entity table = new Entity("testTable", module, EntityType.TABLE);
        DataElementUtils.saveQuiet(table);
        table.setDisplayName( "$project.Name/$module.Name/$entity.Name" );
        final Operation op = Operation.createOperation("Insert", Operation.OPERATION_TYPE_JAVA, table);
        DataElementUtils.saveQuiet(op);
        final Query query = new Query("All records", table);
        DataElementUtils.saveQuiet(query);
        query.setQuery("<@distinct \"name\"/>");
        query.setParametrizingOperation(op);
//TODO
//        project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY )
//                .setSource( "<#macro distinct column>SELECT DISTINCT ${column} FROM ${entity.getName()}</#macro>" );
//        final BeModelCollection<TableRef> tableRefs = table.getOrCreateTableReferences();
//        final TableRef tableRef = new TableRef("ref1", "user", tableRefs);
//        tableRef.setTableTo("users");
//        tableRef.setColumnsTo("userName");
//        DataElementUtils.saveQuiet(tableRef);
//
//        final Path tempFolder = Files.createTempDirectory("be4-temp");
//        Serialization.save( project, tempFolder );
//
//        final Project project2 = Serialization.load( tempFolder );
//        assertEquals(new HashSet<>(Arrays.asList( "Admin", "Guest" )), project2.getRoles());
//        assertEquals( project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY ).getSource(),
//                project2.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY ).getSource() );
//        final Entity table2 = project2.getApplication().getEntity( "testTable" );
//        assertNotNull(table2);
//        assertEquals(table, table2);
//        assertEquals(table2, table);
//        final Query query2 = table2.getQueries().get("All records");
//        assertNotNull(query2);
//        assertEquals("<@distinct \"name\"/>", query2.getQuery());
//        final Operation op2 = table2.getOperations().get("Insert");
//        assertNotNull(op2);
//        assertEquals(op, op2);
//        assertSame(op2, query2.getParametrizingOperation());
//
//        final BeModelCollection<TableRef> tableReferences = project.getEntity("testTable").getOrCreateTableReferences();
//        final BeModelCollection<TableRef> tableReferences2 = project2.getEntity("testTable").getOrCreateTableReferences();
//        assertEquals(tableReferences.getSize(), tableReferences2.getSize());
//        assertEquals(tableReferences.iterator().next().getColumnsFrom(), tableReferences2.iterator().next().getColumnsFrom());
//
//        FileUtils.deleteRecursively( tempFolder );
    }

    /**
     * Unexpected error when serializing 'connectionUrl' of TestProject/Connection profiles/Local/test
     * on windows
     * @throws Exception
     */
    @Test
    public void testWriteReadConnectionProfile() throws Exception
    {
        final Project project = new Project("TestProject");
        BeConnectionProfile profile = new BeConnectionProfile( "test", project.getConnectionProfiles().getLocalProfiles() );
        profile.setConnectionUrl( "jdbc:db2://localhost:50000/housing:retrieveMessagesFromServerOnGetMessage=true;" );
        profile.setUsername( "test" );
        profile.setPassword( "password" );
        LinkedHashMap<String, Object> serializedProfiles = new LinkedHashMap<>();
        serializedProfiles.put( profile.getName(), YamlSerializer.serializeProfile( profile ) );
        String serialized = new Yaml().dump( serializedProfiles );
        
        LoadContext loadContext = new LoadContext();
        BeConnectionProfile createdProfile = YamlDeserializer.deserializeConnectionProfile( loadContext, serialized, project );
        assertNotNull(createdProfile);
        if(!loadContext.getWarnings().isEmpty())
            throw loadContext.getWarnings().get( 0 );
        assertEquals(profile.getConnectionUrl(), createdProfile.getConnectionUrl());
        assertEquals(profile.getUsername(), createdProfile.getUsername());
    }

    @Test
    public void testWriteReadCustomizedModule() throws Exception
    {
        final Project project = new Project("TestProject");
        final Module module = new Module("testmodule", project.getModules());
        DataElementUtils.saveQuiet( module );
        final Entity table = new Entity("customizedtable", module, EntityType.TABLE);
        DataElementUtils.saveQuiet( table );
        final Operation op1 = Operation.createOperation("normop", Operation.OPERATION_TYPE_JAVA, table);
        assertFalse(module.isCustomized());
        DataElementUtils.saveQuiet( op1 );
        final Operation op2 = Operation.createOperation("custop", Operation.OPERATION_TYPE_JAVA, table);
        op2.setOriginModuleName( Project.APPLICATION );
        DataElementUtils.saveQuiet( op2 );
        final Query q1 = new Query("normq", table);
        DataElementUtils.saveQuiet( q1 );
        final Query q2 = new Query("custq", table);
        q2.setOriginModuleName( Project.APPLICATION );
        DataElementUtils.saveQuiet( q2 );
        assertTrue(module.isCustomized());
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );
        
        final Project project2 = Serialization.load( tempFolder );
        final Module readModule = project2.getModule( "testmodule" );
        assertNotNull(readModule);
        assertTrue(readModule.isCustomized());
        final Entity readTable = readModule.getEntity( "customizedtable" );
        assertNotNull(readTable);
        assertTrue(readTable.isCustomized());
        assertEquals(1, readTable.getOperations().getSize());
        assertNotNull(readTable.getOperations().get("custop"));
        assertEquals(1, readTable.getQueries().getSize());
        assertNotNull(readTable.getQueries().get("custq"));
        
        FileUtils.deleteRecursively( tempFolder );
    }

    @Test
    public void testWriteReadQueryOperation() throws Exception
    {
        final Project project = new Project("TestProject");
        final Module module = project.getApplication();
        DataElementUtils.saveQuiet( module );
        final Entity table = new Entity("table", module, EntityType.TABLE);
        DataElementUtils.saveQuiet( table );
        final Operation op = Operation.createOperation("op", Operation.OPERATION_TYPE_JAVA, table);
        DataElementUtils.saveQuiet( op );
        final Query query = new Query("q", table);
        DataElementUtils.saveQuiet( query );
        query.getOperationNames().add( "op" );
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        assertEquals( "op", readEntity.getQueries().get( "q" ).getOperationNames().getValuesArray()[0] );
        
        FileUtils.deleteRecursively( tempFolder );
    }

    @Test
    public void testWriteReadOperationExtender() throws Exception
    {
        final Project project = new Project("TestProject");
        final Module module = project.getApplication();
        DataElementUtils.saveQuiet( module );
        final Entity table = new Entity("table", module, EntityType.TABLE);
        DataElementUtils.saveQuiet( table );
        final Operation op = Operation.createOperation("op", Operation.OPERATION_TYPE_JAVA, table);
        DataElementUtils.saveQuiet( op );
        final OperationExtender ex1 = new OperationExtender( op, module.getName() );
        DataElementUtils.saveQuiet( ex1 );
        ex1.setClassName( "test.class.name" );
        ex1.setInvokeOrder( 1 );
        final JavaScriptOperationExtender ex2 = new JavaScriptOperationExtender( op, module.getName() );
        DataElementUtils.saveQuiet( ex2 );
        ex2.setFileName( "MyExtender.js" );
        ex2.setCode( "Hello world!" );
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        final BeModelCollection<OperationExtender> extenders = readEntity.getOperations().get( "op" ).getExtenders();
        assertEquals(2, extenders.getSize());
        final OperationExtender readEx1 = extenders.get( "application - 0001" );
        assertEquals("test.class.name", readEx1.getClassName());
        assertEquals(1, readEx1.getInvokeOrder());
        final JavaScriptOperationExtender readEx2 = ( JavaScriptOperationExtender ) extenders.get( "application - 0002" );
        assertEquals(0, readEx2.getInvokeOrder());
        assertEquals("MyExtender.js", readEx2.getFileName());
        assertEquals("Hello world!", readEx2.getCode());
        
        FileUtils.deleteRecursively( tempFolder );
    }

    @Test
    public void testWriteReadQueryFilters() throws Exception
    {
        final Project project = new Project("TestProject");
        final Module module = project.getApplication();
        DataElementUtils.saveQuiet( module );
        final Entity table = new Entity("table", module, EntityType.TABLE);
        DataElementUtils.saveQuiet( table );
        final Query query = new Query("q", table);
        DataElementUtils.saveQuiet( query );
        final Query query2 = new Query("q2", table);
        DataElementUtils.saveQuiet( query2 );
        final Query filterQuery = new Query("filter query", table);
        DataElementUtils.saveQuiet( filterQuery );
        final QuickFilter qf1 = new QuickFilter( "filter1", query );
        qf1.setTargetQueryName( filterQuery.getName() );
        qf1.setQueryParam( "param" );
        qf1.setFilteringClass( "test.class" );
        DataElementUtils.saveQuiet( qf1 );
        final QuickFilter qf2 = new QuickFilter( "filter2", query2 );
        qf2.setTargetQueryName( filterQuery.getName() );
        qf2.setQueryParam( "param2" );
        DataElementUtils.saveQuiet( qf2 );
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        assertEquals( "filter1", readEntity.getQueries().get( "q" ).getQuickFilters()[0].getName() );
        assertEquals( "param", readEntity.getQueries().get( "q" ).getQuickFilters()[0].getQueryParam() );
        assertEquals( "test.class", readEntity.getQueries().get( "q" ).getQuickFilters()[0].getFilteringClass() );
        assertSame( readEntity.getQueries().get("filter query"), readEntity.getQueries().get( "q" ).getQuickFilters()[0].getTargetQuery() );
        assertEquals( "filter2", readEntity.getQueries().get( "q2" ).getQuickFilters()[0].getName() );
        assertEquals( "param2", readEntity.getQueries().get( "q2" ).getQuickFilters()[0].getQueryParam() );
        assertNull( readEntity.getQueries().get( "q2" ).getQuickFilters()[0].getFilteringClass() );
        assertSame( readEntity.getQueries().get("filter query"), readEntity.getQueries().get( "q2" ).getQuickFilters()[0].getTargetQuery() );
        
        FileUtils.deleteRecursively( tempFolder );
    }

    @Test
    public void testWriteReadQuerySettings() throws Exception
    {
        final Project project = new Project("TestProject");
        project.setRoles( Arrays.asList( "Admin", "Guest", "User" ) );
        final Module module = project.getApplication();
        DataElementUtils.saveQuiet( module );
        final Entity table = new Entity("table", module, EntityType.TABLE);
        DataElementUtils.saveQuiet( table );
        final Query query = new Query("q", table);
        DataElementUtils.saveQuiet( query );
        final Query query2 = new Query("q2", table);
        DataElementUtils.saveQuiet( query2 );
        final QuerySettings set1 = new QuerySettings( query );
        set1.setColorSchemeID( 1L );
        set1.getRoles().setValues( project.getRoles() );
        set1.setBeautifier( "my.beautifier" );
        query.setQuerySettings( new QuerySettings[] {set1} );
        final QuerySettings set2 = new QuerySettings( query2 );
        set2.setColorSchemeID( null );
        set2.setAutoRefresh( 30 );
        set2.getRoles().setValues( Arrays.asList( "Admin", "Guest" ) );
        final QuerySettings set3 = new QuerySettings( query2 );
        set3.setColorSchemeID( 1L );
        set3.getRoles().setValues( Arrays.asList( "User" ) );
        query2.setQuerySettings( new QuerySettings[] {set2, set3} );
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        QuerySettings[] querySettings = readEntity.getQueries().get("q").getQuerySettings();
        assertEquals(1, querySettings.length);
        assertEquals((Long)1L, querySettings[0].getColorSchemeID());
        assertEquals("my.beautifier", querySettings[0].getBeautifier());
        assertEquals(new HashSet<>(project.getRoles()), querySettings[0].getRoles().getFinalRoles());
        querySettings = readEntity.getQueries().get("q2").getQuerySettings();
        assertEquals(2, querySettings.length);
        assertEquals(null, querySettings[0].getColorSchemeID());
        //TODO assertEquals(null, querySettings[0].getBeautifier());
        assertEquals(30, querySettings[0].getAutoRefresh());
        assertEquals( new HashSet<>( Arrays.asList( "Admin", "Guest" ) ), querySettings[0].getRoles().getFinalRoles() );
        assertEquals((Long)1L, querySettings[1].getColorSchemeID());
        //TODO assertEquals(null, querySettings[1].getBeautifier());
        assertEquals(0, querySettings[1].getAutoRefresh());
        assertEquals( new HashSet<>( Arrays.asList( "User" ) ), querySettings[1].getRoles().getFinalRoles() );
        
        FileUtils.deleteRecursively( tempFolder );
    }

    @Test
    public void testWriteReadLocalizations() throws Exception
    {
        final Project project = new Project("test");
        final Localizations localizations = project.getApplication().getLocalizations();
        localizations.addLocalization( "en", "entity", Arrays.asList("topic"), "hello", "Hello!" );
        localizations.addLocalization( "de", "entity", Arrays.asList("topic", "topic2"), "hello", "Guten Tag!" );
        localizations.addLocalization( "it", "entity", Arrays.asList("topic2"), "hello", "Buon giorno!" );
        
        final Path tempFolder = Files.createTempDirectory("be4-temp");
        Serialization.save( project, tempFolder );
        
        final Project project2 = Serialization.load( tempFolder );
        final Localizations localizations2 = project2.getApplication().getLocalizations();
        assertEquals("Hello!", localizations2.get( "en" ).get("entity").elements().iterator().next().getValue());
        assertEquals("Guten Tag!", localizations2.get( "de" ).get("entity").elements().iterator().next().getValue());
        assertEquals("Buon giorno!", localizations2.get( "it" ).get("entity").elements().iterator().next().getValue());
        
        FileUtils.deleteRecursively( tempFolder );
    }
} 
