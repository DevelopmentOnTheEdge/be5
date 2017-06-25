package com.developmentontheedge.be5.metadata.util;

import java.util.logging.Logger;

public class JULLogger implements ProcessController
{
	protected Logger log; 
    
    public JULLogger(Logger log)
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

    public static String infoBlock(String info){
        return "------------------------------------------------------------------" +
                "\n" + info +
                "\n------------------------------------------------------------------------";
    }
}
