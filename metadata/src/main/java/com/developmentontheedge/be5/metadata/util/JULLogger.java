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

//    public static String infoBlock(String level, String info)
//    {
//        return "[" + level + "] " + IntStream.range(0,72 - level.length() - 3).map(ch -> "-").collect(Collectors.joining()) +
//                "\n" + info +
//                "\n------------------------------------------------------------------------";
//    }
}
