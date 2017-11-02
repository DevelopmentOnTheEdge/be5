package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.junit.Test;

import java.util.logging.Logger;


public class AppValidateTest extends TestUtils
{
    @Test
    public void validate() throws Exception
    {
        new AppValidate()
                .setLogger(new JULLogger(Logger.getLogger(AppValidateTest.class.getName())))
                .setProjectPath(path.toAbsolutePath().toString())
                .execute();
    }

}