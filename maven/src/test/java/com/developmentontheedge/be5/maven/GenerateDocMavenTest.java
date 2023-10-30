package com.developmentontheedge.be5.maven;

import java.io.File;
import org.junit.Test;

public class GenerateDocMavenTest
{
	@Test
    public void validate()
    {
    	GenerateDocMojo mojo = new GenerateDocMojo();

        mojo.projectPath = new File("target/test-classes/project");
    	mojo.docPath     = new File("target/test-classes/doc/source");

System.out.println("Project: " + mojo.projectPath.getAbsolutePath());
System.out.println("Doc    : " + mojo.docPath.getAbsolutePath());

    	mojo.execute();
    }
}
