package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.server.services.DaemonStarter;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

import javax.servlet.ServletContextEvent;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;


public abstract class Be5GuiceServletContextListener extends GuiceServletContextListener
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public Be5GuiceServletContextListener()
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
    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        Injector injector = (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
        injector.getInstance(DaemonStarter.class).shutdown();

        super.contextDestroyed(servletContextEvent);
    }
}
