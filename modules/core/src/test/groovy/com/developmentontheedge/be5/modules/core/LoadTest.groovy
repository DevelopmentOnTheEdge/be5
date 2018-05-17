package com.developmentontheedge.be5.modules.core

import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.modules.core.controllers.CoreBe5ProjectTest

import javax.inject.Inject
import com.developmentontheedge.be5.maven.AppValidate
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException
import com.developmentontheedge.be5.metadata.model.Module
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection
import org.junit.Test


import static org.junit.Assert.assertEquals


class LoadTest extends CoreBe5ProjectTest
{
    @Inject ProjectProvider projectProvider

    @Test
    void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        assertEquals("core", projectProvider.getProject().getAppName())

        BeModelCollection<Module> modules = projectProvider.getProject().getModules()
        assertEquals(0, modules.getSize())
    }

    @Test
    void validate()
    {
        new AppValidate().setBe5ProjectPath("./").execute()
    }
}
