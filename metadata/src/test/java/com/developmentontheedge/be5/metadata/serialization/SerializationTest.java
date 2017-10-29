package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.TestProjectUtils;
import one.util.streamex.StreamEx;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private TestProjectUtils utils = new TestProjectUtils();

    @Test
    public void testSerializationBasics() throws IOException, ProjectSaveException, ProjectLoadException
    {
        Path path = tmp.newFolder().toPath();
        Project project = utils.getProject("test");
        Entity entity = utils.createEntity( project, "entity", "ID" );
        utils.createScheme( entity );
        utils.createQuery( entity );
        utils.createOperation( entity );

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
        assertEquals( StreamEx.of( "Administrator", "Operator" ).toSet(), entity2.getQueries().get( "query" ).getRoles().getFinalValues() );
        assertEquals( "op", entity2.getQueries().get( "query" ).getOperationNames().getFinalValuesString() );
    }

}
