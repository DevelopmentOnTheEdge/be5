package com.developmentontheedge.be5.metadata.serialization;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import one.util.streamex.StreamEx;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.exception.ProjectSaveException;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.PageCustomization;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.SpecialRoleGroup;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

public class SerializationTest
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testSerializationBasics() throws IOException, ProjectSaveException, ProjectLoadException
    {
        Path path = tmp.newFolder().toPath();
        Project project = new Project( "test" );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        Entity entity = createEntity( project );
        createScheme( entity );
        createQuery( entity );
        createOperation( entity );        
        
        Serialization.save( project, path );
        assertEquals(path, project.getLocation());
        LoadContext lc = new LoadContext();
        
        Project project2 = Serialization.load( path, lc );
        project2.setDatabaseSystem( Rdbms.POSTGRESQL );
        lc.check();
        Entity entity2 = project2.getEntity( "entity" );
        assertEquals(entity, entity2);
        assertTrue(entity2.isBesql());
        assertEquals("VARCHAR(20)", entity2.findTableDefinition().getColumns().get("CODE2").getTypeString());
        assertEquals( StreamEx.of( "Administrator", "Operator" ).toSet(), entity2.getQueries().get( "query" ).getRoles().getFinalValues() );
        assertEquals( "op", entity2.getQueries().get( "query" ).getOperationNames().getFinalValuesString() );
    }

    private TableDef createScheme( Entity entity )
    {
        TableDef scheme = new TableDef( entity );
        DataElementUtils.save(scheme);
        ColumnDef column = new ColumnDef( "CODE", scheme.getColumns() );
        column.setTypeString( "VARCHAR(20)" );
        column.setCanBeNull( true );
        column.setPrimaryKey( true );
        DataElementUtils.save(column);
        ColumnDef column2 = new ColumnDef( "CODE2", scheme.getColumns() );
        column2.setTableTo( entity.getName() );
        column2.setColumnsTo( column.getName() );
        DataElementUtils.save(column2);
        return scheme;
    }

    private Operation createOperation( Entity entity )
    {
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_GROOVY, entity );
        DataElementUtils.save( operation );
        PageCustomization customization = new PageCustomization( PageCustomization.DOMAIN_OPERATION, PageCustomization.TYPE_JS,
                operation.getOrCreateCollection( PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        customization.setCode( "alert('!!!')" );
        DataElementUtils.save( customization );
        return operation;
    }

    private Query createQuery( Entity entity )
    {
        Query query = new Query( "query", entity );
        query.getRoles().add( '@'+SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP );
        query.getRoles().addExclusion( "User" );
        query.getOperationNames().setValues( Collections.singleton( "op" ) );
        DataElementUtils.save(query);
        return query;
    }

    private Entity createEntity( Project project )
    {
        Entity entity = new Entity( "entity", project.getApplication(), EntityType.TABLE );
        entity.setPrimaryKey( "CODE" );
        entity.setBesql( true );
        DataElementUtils.save(entity);
        return entity;
    }
}
