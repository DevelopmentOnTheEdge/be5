package com.developmentontheedge.be5.inject;

import java.nio.file.Path;

/**
 * Everything that can be accessed when the module is loaded.
 * 
 * @author asko
 */
public interface InitializerContext
{

    /**
     * Resolve path within the servlet
     * 
     * @param path relative path
     * 
     * @return absolute path
     */
    Path resolvePath(String path);
}
