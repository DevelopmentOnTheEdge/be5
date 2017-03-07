package com.developmentontheedge.be5.api.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;

import com.developmentontheedge.be5.api.InitializerContext;

public class InitializerContextImpl implements InitializerContext
{
    private final ServletConfig config;

    public InitializerContextImpl(ServletConfig config)
    {
        this.config = config;
    }

    @Override
    public Path resolvePath(String path)
    {
        return Paths.get(config.getServletContext().getRealPath( path ));
    }
    
}
