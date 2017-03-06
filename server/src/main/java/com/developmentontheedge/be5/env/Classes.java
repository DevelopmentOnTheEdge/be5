package com.developmentontheedge.be5.env;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class Classes
{

    public static <T> Class<? extends T> tryLoad(String moduleName, String className, Class<T> klass) throws Be5Exception
    {
        Bundle moduleBundle = Platform.getBundle(moduleName);
        
        if (moduleBundle == null)
        {
            throw Be5Exception.internal("Can't find bundle '" + moduleName + "'");
        }
        
        try
        {
            return moduleBundle.loadClass(className).asSubclass(klass);
        }
        catch (ClassNotFoundException | IllegalStateException | ClassCastException e2)
        {
            throw Be5Exception.internal(e2);
        }
    }

    /**
     * Returns a list of bundles that potentially can contain this class.
     * @return a list of bundles names, there's no guarantee that any of them contains the class
     */
    private static List<String> tryGuessSourceBundle(String className) {
        String[] classNameParts = Iterables.toArray(Splitter.on('.').split(className), String.class);
        
        // module?
        String moduleClassPrefix = "com.beanexplorer.business.";
        String modulePrefix = "com.beanexplorer.module.";
        if (classNameParts.length > 4 && className.startsWith(moduleClassPrefix))
        {
            String moduleName = modulePrefix + classNameParts[3];
            return ImmutableList.of(moduleName);
        }
        if (classNameParts.length >= 4 && className.startsWith("com.beanexplorer."))
        {
    		return ImmutableList.of(modulePrefix + classNameParts[2],
    				"com.beanexplorer");
        }
        
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<classNameParts.length-1; i++)
        {
        	if(i > 0) sb.append('.');
        	sb.append(classNameParts[i]);
        	result.add(sb.toString());
        }

        /*
         * for case find com.developmentontheedge.condo.lk.Klass in
         * [ com.developmentontheedge.condo.lk, com.developmentontheedge.condo ]
         */
        result.sort((o1, o2) -> Integer.compare(o2.length(),o1.length()));

        return result;
    }

    private static <T> Class<? extends T> tryGuessSourceBundleAndLoadClass(String className, Class<T> klass) {
        List<String> moduleNames = tryGuessSourceBundle(className);
        
        for(String moduleName : moduleNames)
        {
        	if(Platform.getBundle(moduleName) == null)
        		continue;
    		return tryLoad(moduleName, className, klass);
        }
        throw Be5Exception.internal("No suitable source bundles found for '" + className + "'");
    }

    /**
     * Tries to load a class.
     * @throws Be5Exception if something wrong occurred
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> tryLoad(String className, Class<T> klass) throws Be5Exception {
        try
        {
            return (Class<T>) Class.forName(className); // ClassCastException is caught
        }
        catch (ClassNotFoundException e)
        {
            return tryGuessSourceBundleAndLoadClass(className, klass);
        }
        catch (ClassCastException e)
        {
            throw Be5Exception.internal(e);
        }
    }

    /**
     * Tries to load a class and create an instance of it using the default constructor.
     * @throws Be5Exception if something wrong occurred
     */
    public static <T> T tryInstantiate(String className, Class<T> klass) throws Be5Exception {
        try
        {
            return tryLoad(className, klass).newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw Be5Exception.internal(e);
        }
    }

}
