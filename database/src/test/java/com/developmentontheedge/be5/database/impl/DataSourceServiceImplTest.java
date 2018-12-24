package com.developmentontheedge.be5.database.impl;


import com.developmentontheedge.be5.base.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.database.DatabaseTest;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import com.developmentontheedge.sql.format.dbms.Dbms;
import com.google.inject.Stage;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class DataSourceServiceImplTest extends DatabaseTest
{
    @Inject
    private Stage stage;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Path path;

    @Before
    public void setUp() throws Exception
    {
        path = tmp.newFolder().toPath();
        Project project = ProjectTestUtils.getProject("test");
        ProjectTestUtils.createH2Profile(project, "DataSourceServiceImplTest");
        project.setConnectionProfileName("DataSourceServiceImplTest");
        Serialization.save(project, path);
        ModuleLoader2.loadAllProjects(Collections.singletonList(path.resolve("project.yaml").toUri().toURL()));
    }

    @Test
    public void test() throws Exception
    {
        ProjectProviderImpl projectProvider = new ProjectProviderImpl(stage);
        projectProvider.start();
        DataSourceServiceImpl dataSourceService = new DataSourceServiceImpl(projectProvider);
        dataSourceService.start();
        BasicDataSource dataSource = (BasicDataSource) dataSourceService.getDataSource();
        assertEquals("jdbc:h2:mem:DataSourceServiceImplTest;DB_CLOSE_DELAY=-1", dataSource.getUrl());
        assertEquals("sa", dataSource.getUsername());
        assertEquals(Dbms.H2, dataSourceService.getDbms());
    }

}
