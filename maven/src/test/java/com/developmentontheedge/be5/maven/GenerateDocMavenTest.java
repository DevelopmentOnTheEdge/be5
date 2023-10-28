package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.NullLogger;

import java.io.File;

import org.junit.Before;
import org.junit.Test;


public class GenerateDocMavenTest extends TestMavenUtils
{
    @Test
    public void validate()
    {
    	GenerateDocMojo mojo = new GenerateDocMojo();

        mojo.projectPath = tpmProjectPath.toFile();
    	mojo.docPath = new File("target/test-classes/doc");
    	//mojo.logger = new NullLogger();
    	
    	//mojo.projectPath = new File("C:/projects/sirius/sirius-erp");
    	//mojo.docPath     = new File("C:/projects/sirius/sirius-erp-docs_/source");
      
        mojo.execute();
    }
}
