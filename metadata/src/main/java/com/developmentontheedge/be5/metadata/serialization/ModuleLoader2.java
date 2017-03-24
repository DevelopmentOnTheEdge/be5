package com.developmentontheedge.be5.metadata.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.developmentontheedge.be5.metadata.exception.ProjectLoadException;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.ProjectFileStructure;

public class ModuleLoader2
{
    private static String PROJECT_FILE_NAME = ProjectFileStructure.PROJECT_FILE_NAME_WITHOUT_SUFFIX + ProjectFileStructure.FORMAT_SUFFIX;
    
    private static Map<String, Path> modulesMap;
    
    private static synchronized void init()
    {
        if( modulesMap != null )
            return;

        modulesMap= new HashMap<>();

        try
        {
            Enumeration<URL> urls = (ModuleLoader2.class).getClassLoader().getResources(PROJECT_FILE_NAME);
            URL url;
            while( urls.hasMoreElements() )
            {
                url = urls.nextElement();
                final String name = parse(url);
                
                String ext = url.toExternalForm();
                String jar = ext.substring(0, ext.indexOf('!'));
                FileSystem fs = FileSystems.newFileSystem(URI.create(jar), new HashMap<String, String>());
                Path p = fs.getPath("./");
                System.out.println("ext=" + url.toExternalForm() + ", path=" + p);                

                modulesMap.put(name, fs.getPath("./"));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private static String parse(URL url) throws IOException
    {
        try(
                InputStream in = url.openStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            )
        {
            String ln = r.readLine();
            return ln.substring(0, ln.indexOf(':')).trim();
        }
        catch (IOException x) 
        {
//            System.out.printlnfail( "Error reading configuration file", x);
        }
        
        return null;
    }
    
    public static boolean containsModule(String name)
    {
        init();
        
        return modulesMap.containsKey(name);
    }
    
    public static Path resolveModule(String name)
    {
        init();
        
        return modulesMap.get(name);
    }

    public static Project loadModule(String name, LoadContext context) throws ProjectLoadException
    {
        if( ! containsModule(name))
            throw new IllegalArgumentException("Module not found: " + name);

        return Serialization.load(modulesMap.get(name), true, context );
    }
    
}
