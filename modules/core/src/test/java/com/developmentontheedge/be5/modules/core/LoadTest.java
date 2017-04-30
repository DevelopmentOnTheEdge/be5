package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.metadata.exception.ProjectElementException;
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.Query;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoadTest
{

    @Test
    @Ignore
    public void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
//        Project project = projectProvider.getProject(modulesAndProject);
//        List<Project> modules = projectProvider.getModulesForProject(project, modulesAndProject);
//        assertEquals(1, modules.size());
//        assertEquals("core", modules.get(0).getName());
    }

    @Test
    @Ignore
    public void testQueryCompiledValidateCore() throws ProjectElementException
    {
//        Query testQuery = sp.getProject().getEntity("languages").getQueries().get("All records");
//
//        String validatedQuery = testQuery.getQueryCompiled().validate().trim();
//        assertNotNull(validatedQuery);

    }
}
