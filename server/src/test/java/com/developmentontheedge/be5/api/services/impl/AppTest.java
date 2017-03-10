package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import com.developmentontheedge.be5.metadata.serialization.WatchDir;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppTest
{
    private static volatile boolean dirty = false;
    private static WatchDir watcher = null;
    private static Project project;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        project = Serialization.load(
                Paths.get("src/test/java/com/developmentontheedge/be5/api/services/impl/app").toAbsolutePath(),
                new LoadContext() );
    }

    @Test
    public void testLoadProject()
    {
        assertNotNull(project);
    }

    @Test
    public void testGetQuery()
    {
        assertEquals("    SELECT\n" +
                "      t.ID AS \"___ID\",\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t", project.getApplication().getEntity("testtable").getQueries().get("Test 1D unknown").getQuery());
    }
}
