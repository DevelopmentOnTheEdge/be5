package com.developmentontheedge.be5.maven;

import org.junit.Test;
import org.apache.maven.plugin.logging.Log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MavenLoggerTest
{
    @Test
    public void setOperationName() throws Exception
    {
        Log mock = mock(Log.class);
        MavenLogger logger = new MavenLogger(mock);
        logger.setOperationName("foo");

        verify(mock).info("Operation started: foo");
    }

    @Test
    public void setProgress() throws Exception
    {
        Log mock = mock(Log.class);
        MavenLogger logger = new MavenLogger(mock);
        logger.setProgress(0.5);

        verify(mock).info("  progress: 50.0%");
    }

}