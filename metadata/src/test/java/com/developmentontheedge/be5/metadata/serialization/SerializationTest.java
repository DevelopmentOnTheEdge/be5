package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.GroovyOperation;
import com.developmentontheedge.be5.metadata.model.GroovyOperationExtender;
import com.developmentontheedge.be5.metadata.model.JavaScriptOperationExtender;
import com.developmentontheedge.be5.metadata.model.Localizations;
import com.developmentontheedge.be5.metadata.model.MassChange;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationExtender;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuerySettings;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.model.StaticPage;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.model.TableRef;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlDeserializer;
import com.developmentontheedge.be5.metadata.serialization.yaml.YamlSerializer;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import one.util.streamex.StreamEx;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.Yaml;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static com.developmentontheedge.be5.metadata.util.ProjectTestUtils.*;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SerializationTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testSerializationBasics() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = getProject("test");
        createScript( project, "Post-db", "INSERT INTO entity (name) VALUES ('foo')" );
        createStaticPage(project, "en", "page", "Content");

        MassChange mc = new MassChange( "Query[name*=\"All records\"]", project.getApplication().getMassChangeCollection(),
                Collections.singletonMap( "type", QueryType.D2 ) );
        DataElementUtils.save(mc);

        Entity entity = createEntity( project, "entity", "ID" );
        TableDef scheme = createScheme(entity);

        //only for test SqlColumnType getType( Collection<ColumnDef> stack )
        ColumnDef column3 = new ColumnDef( "column3", scheme.getColumns() );
        column3.setTableTo( entity.getName() );
        column3.setColumnsTo( "ID" );
        DataElementUtils.save(column3);

        Query query = createQuery(entity, "All records", Arrays.asList('@' + SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP, "-User"));
        query.getOperationNames().setValues( Collections.singleton( "op" ) );

        createOperation( entity, "op" );

        Path modulePath = tmp.newFolder().toPath();
        Project moduleProject = createModule(project, "testModule", modulePath);

        final FreemarkerScript script = new FreemarkerScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY, moduleProject.getMacroCollection() );
        script.setSource( "<#macro distinct column>SELECT DISTINCT ${column} FROM ${entity.getName()}</#macro>" );
        DataElementUtils.saveQuiet( script );


        Serialization.save( project, path );
        Serialization.save( moduleProject, modulePath );
        assertEquals(path, project.getLocation());
        //ModuleLoader2.mergeAllModules( project, Collections.singletonList( moduleProject ), lc );

        LoadContext lc = new LoadContext();
        Project project2 = Serialization.load( path, lc );
        project2.setDatabaseSystem( Rdbms.POSTGRESQL );
        lc.check();
        Entity entity2 = project2.getEntity( "entity" );
        assertEquals(entity, entity2);
        assertTrue(entity2.isBesql());
        assertEquals("VARCHAR(20)", entity2.findTableDefinition().getColumns().get("name").getTypeString());
        assertEquals( StreamEx.of( "Administrator", "Operator" ).toSet(), entity2.getQueries().get( "All records" ).getRoles().getFinalValues() );
        assertEquals( "op", entity2.getQueries().get( "All records" ).getOperationNames().getFinalValuesString() );

        assertEquals("INSERT INTO entity (name) VALUES ('foo')",
                project.mergeTemplate( project2.getApplication().getFreemarkerScripts().getScripts().get(0) ).validate());

        assertEquals("Content", project2.getStaticPageContent( "en", "page" ));

        project2.applyMassChanges( lc );
        assertEquals(QueryType.D2, entity2.getQueries().get( "All records" ).getType());

        Project moduleProject2 = Serialization.load( modulePath, lc );

        ArrayList<URL> urls = new ArrayList<>();
        urls.add(modulePath.resolve("project.yaml").toUri().toURL());
        urls.add(path.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls);

        ModuleLoader2.mergeAllModules( project, Collections.singletonList( moduleProject2 ), lc );

        Serialization.loadModuleMacros(project2.getModule("testModule"));
    }

    @Test
    public void testWriteReadCompareProject() throws Exception
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

        final FreemarkerScript script = new FreemarkerScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY, project.getMacroCollection() );
        script.setSource( "" );
        DataElementUtils.saveQuiet( script );

        project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY )
                .setSource( "<#macro distinct column>SELECT DISTINCT ${column} FROM ${entity.getName()}</#macro>" );
        final BeModelCollection<TableRef> tableRefs = table.getOrCreateTableReferences();
        final TableRef tableRef = new TableRef("ref1", "user", tableRefs);
        tableRef.setTableTo("users");
        tableRef.setColumnsTo("userName");
        DataElementUtils.saveQuiet(tableRef);

        final Path tempFolder = tmp.newFolder().toPath();
        Serialization.save( project, tempFolder );

        final Project project2 = Serialization.load( tempFolder );
        assertEquals(new HashSet<>(Arrays.asList( "Admin", "Guest" )), project2.getRoles());
        assertEquals( project.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY ).getSource(),
                project2.getMacroCollection().optScript( FreemarkerCatalog.MAIN_MACRO_LIBRARY ).getSource() );
        final Entity table2 = project2.getApplication().getEntity( "testTable" );
        assertNotNull(table2);
        assertEquals(table, table2);
        assertEquals(table2, table);
        final Query query2 = table2.getQueries().get("All records");
        assertNotNull(query2);
        assertEquals("<@distinct \"name\"/>", query2.getQuery());
        final Operation op2 = table2.getOperations().get("Insert");
        assertNotNull(op2);
        assertEquals(op, op2);
        assertSame(op2, query2.getParametrizingOperation());

        final BeModelCollection<TableRef> tableReferences = project.getEntity("testTable").getOrCreateTableReferences();
        final BeModelCollection<TableRef> tableReferences2 = project2.getEntity("testTable").getOrCreateTableReferences();
        assertEquals(tableReferences.getSize(), tableReferences2.getSize());
        assertEquals(tableReferences.iterator().next().getColumnsFrom(), tableReferences2.iterator().next().getColumnsFrom());
    }

    /**
     * Unexpected error when serializing 'connectionUrl' of TestProject/Connection profiles/Local/test
     * on windows
     * and on opensuse from console
     * @throws Exception
     */
    @Test
    @Ignore
    public void testWriteReadConnectionProfile() throws Exception
    {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));

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

        final Query q1 = new Query("normq", table);
        DataElementUtils.saveQuiet( q1 );

        final Operation op2 = Operation.createOperation("custop", Operation.OPERATION_TYPE_JAVA, table);
        op2.setOriginModuleName( Project.APPLICATION );
        DataElementUtils.saveQuiet( op2 );

        final Query q2 = new Query("custq", table);
        q2.setOriginModuleName( Project.APPLICATION );
        DataElementUtils.saveQuiet( q2 );

        assertTrue(module.isCustomized());

        final Path tempFolder = tmp.newFolder().toPath();
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

        final Path tempFolder = tmp.newFolder().toPath();
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        assertEquals( "op", readEntity.getQueries().get( "q" ).getOperationNames().getValuesArray()[0] );
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
        final GroovyOperationExtender ex3 = new GroovyOperationExtender( op, module.getName() );
        DataElementUtils.saveQuiet( ex3 );
        ex3.setFileName( "test.MyExtender.groovy" );
        ex3.setCode( "Hello world!" );

        final Path tempFolder = tmp.newFolder().toPath();
        Serialization.save( project, tempFolder );

        final Project readProject = Serialization.load( tempFolder );
        final Entity readEntity = readProject.getApplication().getEntity( "table" );
        final BeModelCollection<OperationExtender> extenders = readEntity.getOperations().get( "op" ).getExtenders();
        assertEquals(3, extenders.getSize());
        final OperationExtender readEx1 = extenders.get( "application - 0001" );
        assertEquals("test.class.name", readEx1.getClassName());
        assertEquals(1, readEx1.getInvokeOrder());
        final JavaScriptOperationExtender readEx2 = ( JavaScriptOperationExtender ) extenders.get( "application - 0002" );
        assertEquals(0, readEx2.getInvokeOrder());
        assertEquals("MyExtender.js", readEx2.getFileName());
        assertEquals("Hello world!", readEx2.getCode());

        final GroovyOperationExtender readEx3 = ( GroovyOperationExtender ) extenders.get( "application - 0003" );
        assertEquals(0, readEx3.getInvokeOrder());
        assertEquals("test/MyExtender.groovy", readEx3.getFileName());
        assertEquals("Hello world!", readEx3.getCode());
    }

    @Test
    public void testGroovyFileName() throws Exception
    {
        Project prj = new Project("test");
        Entity e = new Entity( "e", prj.getApplication(), EntityType.TABLE );
        DataElementUtils.save( e );

        final GroovyOperation groovyOp = (GroovyOperation)Operation.createOperation( "groovyOp", Operation.OPERATION_TYPE_GROOVY, e );
        groovyOp.setFileName("test.GroovyOp.groovy");
        groovyOp.setCode("test");
        DataElementUtils.saveQuiet( groovyOp );
        Path path = tmp.newFolder().toPath();
        Serialization.save( prj, path );

        final Project readProject = Serialization.load( path );
        GroovyOperation operation = (GroovyOperation) readProject.getProject().findOperation("e", "groovyOp");
        assertEquals("test/GroovyOp.groovy", operation.getFileName());
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

        final Path tempFolder = tmp.newFolder().toPath();
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

        final Path tempFolder = tmp.newFolder().toPath();
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
    }

    @Test
    public void testWriteReadLocalizations() throws Exception
    {
        final Project project = new Project("test");
        final Localizations localizations = project.getApplication().getLocalizations();
        localizations.addLocalization( "en", "entity", Arrays.asList("topic"), "hello", "Hello!" );
        localizations.addLocalization( "de", "entity", Arrays.asList("topic", "topic2"), "hello", "Guten Tag!" );
        localizations.addLocalization( "it", "entity", Arrays.asList("topic2"), "hello", "Buon giorno!" );

        final Path tempFolder = tmp.newFolder().toPath();
        Serialization.save( project, tempFolder );

        final Project project2 = Serialization.load( tempFolder );
        final Localizations localizations2 = project2.getApplication().getLocalizations();
        assertEquals("Hello!", localizations2.get( "en" ).get("entity").elements().iterator().next().getValue());
        assertEquals("Guten Tag!", localizations2.get( "de" ).get("entity").elements().iterator().next().getValue());
        assertEquals("Buon giorno!", localizations2.get( "it" ).get("entity").elements().iterator().next().getValue());
    }

    @Test
    public void testStaticPageWithPageCustomization() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = getProject("test");
        StaticPage staticPage = createStaticPage(project, "en", "page", "Content");

        PageCustomization pcSp = new PageCustomization( "css", PageCustomization.DOMAIN_OPERATION_BUTTONS_HEADER, staticPage.getOrCreateCollection(
                PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        DataElementUtils.save( pcSp );

        Serialization.save( project, path );

        LoadContext lc = new LoadContext();
        Project project2 = Serialization.load( path, lc );
        project2.setDatabaseSystem( Rdbms.POSTGRESQL );
        lc.check();

        assertEquals("Content", project2.getStaticPageContent( "en", "page" ));
    }

}
