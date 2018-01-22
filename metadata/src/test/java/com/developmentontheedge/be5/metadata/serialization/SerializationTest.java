package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testSerializationBasics() throws IOException, ProjectSaveException, ProjectLoadException
    {
        Path path = tmp.newFolder().toPath();
        Project project = ProjectTestUtils.getProject("test");
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

        Serialization.save( project, path );
        assertEquals(path, project.getLocation());
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
    }

}
