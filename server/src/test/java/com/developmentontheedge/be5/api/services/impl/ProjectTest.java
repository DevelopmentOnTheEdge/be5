package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectTest extends AbstractProjectTest
{

    @Test
    public void testGetQuery()
    {
        Project project = injector.getProject();
        assertEquals("    SELECT\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t", project.getEntity("testtable").getQueries().get("All records").getQuery());

    }

    @Test
    public void testQueryCompiledValidate() throws ProjectElementException
    {
        Query testQuery = injector.getProject().getEntity("testtable").getQueries().get("All records");

        String validatedQuery = testQuery.getQueryCompiled().validate().trim();
        assertNotNull(validatedQuery);
        assertEquals("SELECT\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t", validatedQuery);
    }

}
