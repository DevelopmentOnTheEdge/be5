package com.developmentontheedge.be5.metadata.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import one.util.streamex.StreamEx;

import org.apache.tools.ant.BuildException;

import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.util.Files2;
import com.developmentontheedge.be5.metadata.util.ModuleUtils;

public class AppDistrib extends BETask
{
    private static final Predicate<Path> TRUE = path -> true;

    private File distribDir;

    private static final String CLASS_PATH = "BE4_CLASSPATH";

    @Override
    public void execute() throws BuildException
    {
        initParameters();

        Path distribPath = distribDir.toPath();
        try
        {
            Files.createDirectories( distribPath );
            copyBeanExplorer4( distribPath );
            if ( getProject().getProperty( "BE4_DBMS_DRIVERS" ) != null )
                copyDbmsDrivers( distribPath );
            else
                copyMyDbmsDrivers( distribPath );
        }
        catch ( BuildException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BuildException( e );
        }
    }

    private void copyMyDbmsDrivers( Path distribPath ) throws IOException
    {
        String[] classPath = getProject().getProperty( CLASS_PATH ).split( ";" );
        Path tomcatPath = distribPath.resolve( "tomcat/lib" );
        Files.createDirectories( tomcatPath );
        for(String classPathEntry : classPath) {
            Path path = Paths.get( classPathEntry );
            try(URLClassLoader cl = new URLClassLoader( new URL[] {path.toUri().toURL()} )) {
                if(cl.getResource( "META-INF/services/java.sql.Driver" ) != null) {
                    System.err.println( "Copying into tomcat folder: " + path );
                    Files.copy( path, tomcatPath.resolve( path.getFileName() ) );
                }
            }
        }
    }

    private void copyDbmsDrivers( Path distribPath ) throws IOException
    {
        Path tomcatPath = distribPath.resolve( "tomcat/lib" );
        Files.createDirectories( tomcatPath );
        for ( String classPathEntry : getProject().getProperty( "BE4_DBMS_DRIVERS" ).split( ";" ) )
        {
            if ( classPathEntry.endsWith( ".jar" ) )
            {
                Path path = Paths.get( classPathEntry );
                Files.copy( path, tomcatPath.resolve( path.getFileName() ) );
            }
        }
    }

    private void copyBeanExplorer4( Path distribPath ) throws IOException
    {
        Path bePath = distribPath.resolve( "projects/java/be4" );
        Path sourcePath = ModuleUtils.getBasePath();
        Path be4path = Paths.get( getProject().getProperty( "BE4_DIR" ) );
        Path prefix = be4path.toAbsolutePath().normalize();
        String[] classPath = getProject().getProperty( CLASS_PATH ).split( ";" );
        for ( String classPathEntry : classPath )
        {
            String relativePath = prefix.relativize( Paths.get( classPathEntry ).toAbsolutePath().normalize() ).toString();
            if ( relativePath.startsWith( "/" ) || relativePath.startsWith( ".." ) )
                throw new IllegalStateException( "ClassPath entry is invalid: " + classPathEntry + "\nBE4_DIR=" + prefix + "\nRelativePath="
                    + relativePath );
            if ( relativePath.endsWith( ".jar" ) )
                copy( sourcePath, bePath, relativePath, TRUE );
            else
                copy( sourcePath, bePath, relativePath, Files2.byExtension( "class" ) );
        }
        copy( sourcePath, bePath, "macro", TRUE );
        copy( sourcePath, bePath, "src/be4lib.xml", TRUE );
        Files.write(
                bePath.resolve( "src/ant.properties" ),
                ( CLASS_PATH + "=" + StreamEx
                        .of( classPath )
                        .map( p -> be4path.resolve( prefix.relativize( Paths.get( p ).toAbsolutePath().normalize() ) ).toString()
                                .replace( '\\', '/' ) ).joining( ";" ) ).getBytes( StandardCharsets.UTF_8 ) );
        Files.write( bePath.resolve( "src/build.xml" ),
                "<?xml version=\"1.0\"?><project name=\"be4\" default=\"stub\" basedir=\".\"><target name=\"stub\"/></project>"
                        .getBytes( StandardCharsets.UTF_8 ) );

        for ( Module module : beanExplorerProject.getModules() )
        {
            if ( ModuleUtils.isModuleExist( module.getName() ) )
            {
                copy( sourcePath, bePath, "modules/" + module.getName() + "/src", Files2.byExtension( "java" ).negate() );
                copy( sourcePath, bePath, "modules/" + module.getName() + "/project.yaml", Files2.byExtension( "java" ).negate() );
            }
        }
    }

    private void copy( Path baseSource, Path baseTarget, String addPath, Predicate<Path> predicate ) throws IOException
    {
        Path source = baseSource.resolve( addPath );
        Path target = baseTarget.resolve( addPath );
        Files.createDirectories( target );
        Files2.copyAll( source, target, predicate );
    }

    public File getDistribDir()
    {
        return distribDir;
    }

    public void setDistribDir( File distribDir )
    {
        this.distribDir = distribDir;
    }
}
