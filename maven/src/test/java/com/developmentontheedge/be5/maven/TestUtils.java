package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.util.TestProjectUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public abstract class TestUtils
{
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    TestProjectUtils utils = new TestProjectUtils();
    Path path;
    Project project;

    @Before
    public void setUp() throws Exception
    {
        path = tmp.newFolder().toPath();
        project = utils.getProject("test");
        Entity entity = utils.createEntity( project, "entity", "ID" );
        utils.createScheme( entity );
        //utils.createQuery( entity );
        //utils.createOperation( entity );
        Serialization.save( project, path );
    }

    void createTestDB() throws Exception
    {
        initH2Connection(project);

        AppDb appDb = new AppDb();
        appDb.setBe5Project(project);
        appDb.execute();

        assertEquals(1, appDb.getCreatedTables());
        assertEquals(0, appDb.getCreatedViews());

        appDb.connector.executeInsert("INSERT INTO entity (name) VALUES ('bar')");
    }

    void initH2Connection(Project project)
    {
        String profileForIntegrationTests = "profileTestMavenPlugin";
        BeConnectionProfile profile = new BeConnectionProfile(profileForIntegrationTests, project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl("jdbc:h2:~/"+ profileForIntegrationTests);
        profile.setUsername("sa");
        profile.setPassword("");
        profile.setDriverDefinition(Rdbms.H2.getDriverDefinition());
        DataElementUtils.save(profile);
        project.setConnectionProfileName(profileForIntegrationTests);

        project.setDatabaseSystem(Rdbms.H2);
    }

}
