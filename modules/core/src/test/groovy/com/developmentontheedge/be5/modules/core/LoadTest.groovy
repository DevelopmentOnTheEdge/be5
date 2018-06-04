package com.developmentontheedge.be5.modules.core

import com.developmentontheedge.be5.base.services.ProjectProvider
import com.developmentontheedge.be5.metadata.exception.ProjectLoadException
import com.developmentontheedge.be5.metadata.model.Module
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection
import com.developmentontheedge.be5.metadata.scripts.AppValidate
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class LoadTest extends CoreBe5ProjectDbMockTest
{
    @Inject ProjectProvider projectProvider

    @Test
    void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        assertEquals("core", projectProvider.get().getAppName())

        BeModelCollection<Module> modules = projectProvider.get().getModules()
        assertEquals(1, modules.getSize())
        assertNotNull(modules.get("system"))
    }

    @Test
    void validate()
    {
        new AppValidate().setBe5ProjectPath("./").execute()
    }
}
