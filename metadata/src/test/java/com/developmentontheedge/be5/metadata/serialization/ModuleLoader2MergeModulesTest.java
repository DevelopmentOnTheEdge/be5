package com.developmentontheedge.be5.metadata.serialization;

import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.RoleGroup;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class ModuleLoader2MergeModulesTest
{
    @After
    public void tearDown()
    {
        ModuleLoader2.clearModulesMap();
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Project project;

    @Before
    public void setUp() throws Exception
    {
        Path tpmProjectPath = tmp.newFolder().toPath();
        project = ProjectTestUtils.getProject("test");
//        Entity entity = ProjectTestUtils.createEntity( project, "entity", "ID" );
//        ProjectTestUtils.createScheme( entity );
        //ProjectTestUtils.createScript( project, "delete from entity;\nINSERT INTO entity (name) VALUES ('foo')" );
        ProjectTestUtils.createH2Profile(project, "profileTestMavenPlugin");

//        Query query = ProjectTestUtils.createQuery(entity, "All records", Arrays.asList('@' + SpecialRoleGroup.ALL_ROLES_EXCEPT_GUEST_GROUP, "-User"));
//        query.getOperationNames().setValues( Collections.singleton( "op" ) );

        //ProjectTestUtils.createOperation( entity );

        Path modulePath = tmp.newFolder().toPath();
        Project moduleProject1 = createModuleWithQueryRoles(project, null,"testModule1", "testQuery1","testRole1", modulePath);

        Path modulePath2 = tmp.newFolder().toPath();
        Project moduleProject2 = createModuleWithQueryRoles(project, moduleProject1,"testModule2", "testQuery2", "testRole2", modulePath2);

        project.setRoles(Arrays.asList("testRole1", "testRole2"));
        Serialization.save( project, tpmProjectPath );

        ArrayList<URL> urls = new ArrayList<>();
        urls.add(modulePath.resolve("project.yaml").toUri().toURL());
        urls.add(modulePath2.resolve("project.yaml").toUri().toURL());
        urls.add(tpmProjectPath.resolve("project.yaml").toUri().toURL());
        ModuleLoader2.loadAllProjects(urls);

        LoadContext ctx = new LoadContext();
        ModuleLoader2.mergeAllModules( project, Arrays.asList( moduleProject1, moduleProject2 ), ctx );
    }

    private Project createModuleWithQueryRoles(Project project, Project entityOwner, String moduleName, String queryName, String roleName, Path path) throws Exception
    {
        Project moduleProject = new Project( moduleName, true);

        Entity entity;
        if(entityOwner == null)
        {
            entity = ProjectTestUtils.createEntity( moduleProject, "moduleEntity", "ID" );
        }
        else
        {
            Module appModule = new Module( entityOwner.getName(), moduleProject.getModules() );
            DataElementUtils.save( appModule );
            entity = new Entity( "moduleEntity", appModule, EntityType.TABLE );
            DataElementUtils.save( entity );
        }

        ProjectTestUtils.createScheme( entity );

        ProjectTestUtils.createQuery(entity, queryName, Collections.singletonList('@' + roleName));

        moduleProject.setRoles(Collections.singletonList(roleName));
        setRoleGroups(moduleProject, roleName);

        Module appModule = new Module( moduleName, project.getModules() );
        DataElementUtils.save( appModule );

        Serialization.save( moduleProject, path );

        return moduleProject;
    }

    private void setRoleGroups(Project project, String name)
    {
        BeModelCollection<RoleGroup> groups = project.getRoleGroups();
        RoleGroup management = new RoleGroup( name, groups );
        management.getRoleSet().addInclusionAll(Collections.singletonList(name));
        DataElementUtils.save( management );

        project.fireCodeChanged();
    }

    @Test
    public void resolveRoleGroups()
    {
        assertEquals("testRole1",
                project.getEntity("moduleEntity").getQueries().get("testQuery1").getRoles().getFinalRolesString());

        assertEquals("testRole2",
                project.getEntity("moduleEntity").getQueries().get("testQuery2").getRoles().getFinalRolesString());
    }

}
