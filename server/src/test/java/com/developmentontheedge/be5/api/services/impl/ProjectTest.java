package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.AbstractProjectTest;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.model.ParseResult;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testQueryCompiledValidate() throws ProjectElementException
    {
        ServiceProvider sp = getServiceProvider();

        Query testQuery = sp.getMeta().getQueryIgnoringRoles("testtable", "Test 1D unknown");
        ParseResult queryCompiled = testQuery.getQueryCompiled();
        String validatedQuery = queryCompiled.validate();
        assertEquals("SELECT\n" +
                "      t.ID AS \"___ID\",\n" +
                "      t.name AS \"Name\",\n" +
                "      t.value AS \"Value\"\n" +
                "    FROM\n" +
                "      testtable t", validatedQuery);
    }

}
