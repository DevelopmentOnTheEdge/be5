package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.Bootstrap;
import com.developmentontheedge.be5.logging.LogConfigurator;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContextEvent;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;


public abstract class Be5ServletListener extends GuiceServletContextListener
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private Bootstrap be5Bootstrap;

    protected Stage getStage()
    {
        Stage stage = ModuleLoader2.getDevFileExists() ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        log.info("Stage: " + stage);
        return stage;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        LogConfigurator.configure();
        long startTime = System.currentTimeMillis();
        super.contextInitialized(sce);
        log.info("Injector created in " + (System.currentTimeMillis() - startTime) + " ms.");

        be5Bootstrap = new Bootstrap(getCurrentInjector(sce));
        be5Bootstrap.boot();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        if (be5Bootstrap != null) be5Bootstrap.shutdown();
        super.contextDestroyed(sce);
    }

    private Injector getCurrentInjector(ServletContextEvent servletContextEvent)
    {
        return (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
    }
}
