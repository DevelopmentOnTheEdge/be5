package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.server.Bootstrap;
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

    public Be5ServletListener()
    {
        LogConfigurator.configure();
    }

    protected Stage getStage()
    {
        Stage stage = ModuleLoader2.getDevFileExists() ? Stage.DEVELOPMENT : Stage.PRODUCTION;
        log.info("Stage: " + stage);
        return stage;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        long startTime = System.currentTimeMillis();
        super.contextInitialized(sce);
        long injectorStartupTime = System.currentTimeMillis() - startTime;
        log.info("Be5 injector started in " + injectorStartupTime + " ms.");

        be5Bootstrap = new Bootstrap(getCurrentInjector(sce));
        be5Bootstrap.boot();
        long bootstrappedTime = System.currentTimeMillis() - startTime;
        log.info("Be5 application bootstrapped in " + bootstrappedTime + " ms.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        be5Bootstrap.shutdown();
        super.contextDestroyed(sce);
    }

    private Injector getCurrentInjector(ServletContextEvent servletContextEvent)
    {
        return (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
    }
}
