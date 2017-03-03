package com.developmentontheedge.be5.metadata;

import org.apache.tools.ant.BuildFileTest;

public class AntTest extends BuildFileTest
{
    public static final String BUILD_FILE = "C:\\projects\\java\\be4\\modules\\financial\\src\\build.xml"; 
    
    public AntTest(String name)
    {
        super(name);
    }

    @Override
    public void setUp() 
    {
        // initialize Ant
        configureProject(BUILD_FILE);
    }

    public void testCreateDb() 
    {
        executeTarget("be.validate");
    }

    // 
    // expectLog("use.message", "attribute-text");
    // expectBuildException("use.fail", "Fail requested.");
    // assertLogContaining("Nested Element 1");
}