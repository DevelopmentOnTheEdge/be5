package com.developmentontheedge.be5.modules.core;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.test.AbstractProjectTestH2DB;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class LoadTest extends AbstractProjectTestH2DB
{

    @Test
    public void testLoadModuleCore() throws IOException, URISyntaxException, ProjectLoadException
    {
        assertEquals("core", sp.getProject().getAppName());

        BeModelCollection<Module> modules = sp.getProject().getModules();
        assertEquals(0, modules.getSize());
    }

}
