package com.developmentontheedge.be5.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "test")
public class Test extends AbstractMojo
{
	 @Parameter( property = "test.msg", defaultValue = "Hello, BE5" )
	 private String msg;
	
	 @Parameter ( property = "msg2" )
	 private String msg2;

	 public void execute() throws MojoExecutionException
	 {
        getLog().info(msg + msg2);
	 }
}
