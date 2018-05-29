package com.developmentontheedge.be5.modules.system

import com.developmentontheedge.be5.base.services.ProjectProvider

import javax.inject.Inject
import com.developmentontheedge.be5.maven.AppValidate
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException
import com.developmentontheedge.be5.metadata.model.Module
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection
import org.junit.Test


import static org.junit.Assert.assertEquals


class LoadTest extends SystemBe5ProjectTest
{
    @Inject ProjectProvider projectProvider

    @Test
    void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        assertEquals("system", projectProvider.getProject().getAppName())

        BeModelCollection<Module> modules = projectProvider.getProject().getModules()
        assertEquals(0, modules.getSize())
    }

    @Test
    void validate()
    {
        new AppValidate().setBe5ProjectPath("./").execute()
    }
}
