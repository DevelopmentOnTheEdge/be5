package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.serialization.LoadContext;
import com.developmentontheedge.be5.metadata.serialization.Serialization;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectTest extends AbstractProjectTest
{

    @Test
    public void testGetQuery()
    {
        Project project = getServiceProvider().getProject();
        assertEquals("    SELECT\n" +
                "      t.ID AS \"___ID\",\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t", project.getApplication().getEntity("testtable").getQueries().get("Test 1D unknown").getQuery());
    }
}
