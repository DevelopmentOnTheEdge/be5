package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.ProcessController;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Map;


public abstract class Be5Mojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

//    @Parameter( defaultValue = "${plugin}", readonly = true )
//    private PluginDescriptor descriptor;

    public ProcessController logger = new MavenLogger(getLog());

    @Parameter(property = "BE5_PROJECT_PATH", defaultValue = "./")
    public File projectPath;

    @Parameter(property = "BE5_UNLOCK_PROTECTED_PROFILE")
    protected boolean unlockProtectedProfile = false;

    @Parameter(property = "BE5_DEBUG")
    protected boolean debug = false;

    @Parameter(property = "BE5_LOG_PATH")
    protected File logPath = Paths.get("target/sql").toFile();

    @Parameter(property = "BE5_PROFILE")
    public String connectionProfileName;

    @Parameter(property = "DB_PASSWORD")
    protected String connectionPassword;

    protected void init()
    {
        Map pluginContext = getPluginContext();
        if (pluginContext == null) return;
        final PluginDescriptor pluginDescriptor = (PluginDescriptor) pluginContext.get("pluginDescriptor");
        final ClassRealm classRealm = pluginDescriptor.getClassRealm();
        final File classes = new File(project.getBuild().getOutputDirectory());
        try
        {
            classRealm.addURL(classes.toURI().toURL());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        //addDependenciesToClasspath(project.getArtifactId());
    }

//    private void addDependenciesToClasspath(String artifactId)
//    {
//        for (Object artifactObject : project.getDependencyArtifacts())
//        {
//            Artifact artifact = (Artifact) artifactObject;
//            if (artifact.getArtifactId().equals(artifactId))
//            {
//                try
//                {
//                    final URL url = artifact.getFile().toURI().toURL();
//                    final ClassRealm realm = descriptor.getClassRealm();
//                    realm.addURL(url);
//                }
//                catch (MalformedURLException e)
//                {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }

//    private ClassLoader getClassLoader(MavenProject project)
//    {
//        try
//        {
//            List classpathElements = project.getCompileClasspathElements();
//            classpathElements.add( project.getBuild().getOutputDirectory() );
//            classpathElements.add( project.getBuild().getTestOutputDirectory() );
//            URL urls[] = new URL[classpathElements.size()];
//            for ( int i = 0; i < classpathElements.size(); ++i )
//            {
//                urls[i] = new File( (String) classpathElements.get( i ) ).toURL();
//            }
//            return new URLClassLoader( urls, this.getClass().getClassLoader() );
//        }
//        catch ( Exception e )
//        {
//            getLog().debug( "Couldn't get the classloader." );
//            return this.getClass().getClassLoader();
//        }
//    }
}
