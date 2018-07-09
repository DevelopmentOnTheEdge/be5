package com.developmentontheedge.be5.maven;

import org.junit.Test;


public class AppValidateMavenTest extends TestMavenUtils
{
    @Test
    public void validate() throws Exception
    {
//                .setLogger(new JULLogger(Logger.getLogger(AppValidateTest.class.getName())))

        AppValidateMojo mojo = new AppValidateMojo();

        mojo.projectPath = tpmProjectPath.toFile();

        mojo.execute();
    }

    @Test
    public void validateItems() throws Exception
    {
//        new AppValidateMojo()
//                .setLogger(new JULLogger(Logger.getLogger(AppValidateTest.class.getName())))
//                .setBe5ProjectPath(tpmProjectPath.toAbsolutePath().toString())
//                .setCheckQueryPath("entity.All records")
//                .setDdlPath("entity")
//                .setCheckRoles(true)
//                .execute();
    }

}