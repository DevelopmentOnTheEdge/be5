package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.NullLogger;
import org.junit.Test;

public class AppDataMavenTest extends TestMavenUtils
{
    @Test
    public void data() throws Exception
    {
        createTestDB();

        AppDataMojo mojo = new AppDataMojo();

        mojo.projectPath = tpmProjectPath.toFile();
        mojo.connectionProfileName = profileTestMavenPlugin;
        mojo.script = "Post-db";
        mojo.ignoreMissing = false;
        mojo.logger = new NullLogger();
        mojo.execute();
    }

}
