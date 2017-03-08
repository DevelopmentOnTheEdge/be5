package com.developmentontheedge.be5.servlets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * The class is required to keep the Equinox application alive.
 * 
 * @author asko
 */
public class Application implements IApplication {

    private static final String PLATFORM_CLASS = "com.developmentontheedge.be5.platformClass";
    private static final String EXIT = "com.developmentontheedge.be5.exit";

    @Override
    public Object start(IApplicationContext context) throws Exception {
        System.getProperties().put(PLATFORM_CLASS, Platform.class);
        
        while (true)
        {
            Thread.sleep(1000);
            if ("true".equals(System.getProperties().get(EXIT)))
            {
                System.getProperties().remove(EXIT);
            }
        }
        
        // never exits
    }

    @Override
    public void stop() {
        // do nothing
    }

}
