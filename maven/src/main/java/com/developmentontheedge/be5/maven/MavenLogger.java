package com.developmentontheedge.be5.maven;

import com.developmentontheedge.be5.metadata.util.ProcessController;

import org.apache.maven.plugin.logging.Log;


public class MavenLogger implements ProcessController
{
    private Log log;

    public MavenLogger(Log log)
    {
        this.log = log;
    }

    @Override
    public void setOperationName( String name )
    {
        log.info("Operation started: " + name);
    }

    @Override
    public void setProgress(double progress)
    {
        log.info("  progress: " + progress*100 + "%");
    }
}
