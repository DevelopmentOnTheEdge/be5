package com.developmentontheedge.be5.metadata.util;

import com.developmentontheedge.be5.metadata.model.*;
import com.developmentontheedge.be5.metadata.sql.Rdbms;

import java.util.Arrays;
import java.util.Collections;

public class TestProjectUtils
{
    public Project getProject(String name)
    {
        Project project = new Project( name );
        project.setRoles( Arrays.asList( "Administrator", "Guest", "User", "Operator" ) );
        project.setDatabaseSystem( Rdbms.POSTGRESQL );
        return project;
    }

    public TableDef createScheme(Entity entity)
    {
        TableDef scheme = new TableDef( entity );
        DataElementUtils.save(scheme);
        ColumnDef column = new ColumnDef( "ID", scheme.getColumns() );
        column.setTypeString( "KEYTYPE" );
        column.setAutoIncrement(true);
        column.setPrimaryKey( true );
        DataElementUtils.save(column);

        ColumnDef column2 = new ColumnDef( "name", scheme.getColumns() );
        column2.setTypeString( "VARCHAR(20)" );
        column2.setTableTo( entity.getName() );
        column2.setCanBeNull( true );
        column2.setColumnsTo( column.getName() );
        DataElementUtils.save(column2);
        return scheme;
    }

    public Operation createOperation(Entity entity)
    {
        Operation operation = Operation.createOperation( "op", Operation.OPERATION_TYPE_GROOVY, entity );
        DataElementUtils.save( operation );
        PageCustomization customization = new PageCustomization( PageCustomization.DOMAIN_OPERATION, PageCustomization.TYPE_JS,
                operation.getOrCreateCollection( PageCustomization.CUSTOMIZATIONS_COLLECTION, PageCustomization.class ) );
        customization.setCode( "alert('!!!')" );
        DataElementUtils.save( customization );
        return operation;
    }

    public Query createQuery(Entity entity)
    {
        Query query = new Query( "query", entity );
        query.getRoles().add( '@'+SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP );
        query.getRoles().addExclusion( "User" );
        query.getOperationNames().setValues( Collections.singleton( "op" ) );
        DataElementUtils.save(query);
        return query;
    }

    public Entity createEntity(Project project, String entityName, String primaryKeyName)
    {
        Entity entity = new Entity( entityName, project.getApplication(), EntityType.TABLE );
        entity.setPrimaryKey( primaryKeyName );
        entity.setBesql( true );
        DataElementUtils.save(entity);
        return entity;
    }

    public void createScript(Project project, String sql)
    {
        FreemarkerScript script = new FreemarkerScript("Post-db",
                project.getApplication().getFreemarkerScripts());
        script.setSource(sql);
        DataElementUtils.save( script );
    }

    public void createH2Profile(Project project, String name)
    {
        BeConnectionProfile profile = new BeConnectionProfile(name, project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl("jdbc:h2:~/"+ name);
        profile.setUsername("sa");
        profile.setPassword("");
        profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
        DataElementUtils.save(profile);
    }
}
