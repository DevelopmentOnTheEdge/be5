package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.impl.ProjectProviderImpl;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import com.google.inject.Stage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ProjectTest
{
    private static ProjectProvider projectProvider = new ProjectProviderImpl(Stage.PRODUCTION);

    @Test
    public void testGetQuery()
    {
        Project project = projectProvider.getProject();
        assertEquals("SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t", project.getEntity("testtable").getQueries().get("All records").getQuery());

    }

    @Test
    public void testQueryCompiledValidate() throws ProjectElementException
    {
        Query testQuery = projectProvider.getProject().getEntity("testtable").getQueries().get("All records");

        String validatedQuery = testQuery.getQueryCompiled().validate().trim();
        assertNotNull(validatedQuery);
        assertEquals("SELECT\n" +
                "  t.name AS \"Name\",\n" +
                "  t.value AS \"Value\"\n" +
                "FROM\n" +
                "  testtable t", validatedQuery);
    }

}
