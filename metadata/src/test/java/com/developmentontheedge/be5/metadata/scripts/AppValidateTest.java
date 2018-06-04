package com.developmentontheedge.be5.metadata.scripts;

import com.developmentontheedge.be5.metadata.util.JULLogger;
import org.junit.Test;

import java.util.logging.Logger;


public class AppValidateTest extends TestUtils
{
    @Test
    public void validate()
    {
        new AppValidate()
                .setLogger(new JULLogger(Logger.getLogger(AppValidateTest.class.getName())))
                .setBe5ProjectPath(tpmProjectPath.toAbsolutePath().toString())
                .execute();
    }

    @Test
    public void validateItems()
    {
        new AppValidate()
                .setLogger(new JULLogger(Logger.getLogger(AppValidateTest.class.getName())))
                .setBe5ProjectPath(tpmProjectPath.toAbsolutePath().toString())
                .setCheckQueryPath("entity.All records")
                .setDdlPath("entity")
                .setCheckRoles(true)
                .execute();
    }

}