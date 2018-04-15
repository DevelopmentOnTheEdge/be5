package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.FreemarkerCatalog;
import com.developmentontheedge.be5.metadata.model.FreemarkerScript;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import one.util.streamex.StreamEx;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testSerializationBasics() throws Exception
    {
        Path path = tmp.newFolder().toPath();
        Project project = ProjectTestUtils.getProject("test");
        ProjectTestUtils.createScript( project, "Post-db", "INSERT INTO entity (name) VALUES ('foo')" );

        Entity entity = ProjectTestUtils.createEntity( project, "entity", "ID" );
        TableDef scheme = ProjectTestUtils.createScheme(entity);

        //only for test SqlColumnType getType( Collection<ColumnDef> stack )
        ColumnDef column3 = new ColumnDef( "column3", scheme.getColumns() );
        column3.setTableTo( entity.getName() );
        column3.setColumnsTo( "ID" );
        DataElementUtils.save(column3);

        Query query = ProjectTestUtils.createQuery(entity, "All records", Arrays.asList('@' + SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP, "-User"));
        query.getOperationNames().setValues( Collections.singleton( "op" ) );

        ProjectTestUtils.createOperation( entity );

        Path modulePath = tmp.newFolder().toPath();
        Project moduleProject = ProjectTestUtils.createModule(project, "testModule", modulePath);

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

        Project moduleProject2 = Serialization.load( modulePath, lc );

        ArrayList<URL> urls = new ArrayList<>();
        urls.add(modulePath.resolve("project.yaml").toUri().toURL());
        urls.add(path.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls);

        ModuleLoader2.mergeAllModules( project, Collections.singletonList( moduleProject2 ), lc );

        Serialization.loadModuleMacros(project2.getModule("testModule"));
    }

}
