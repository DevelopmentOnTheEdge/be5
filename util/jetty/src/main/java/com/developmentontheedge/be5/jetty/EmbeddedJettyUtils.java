package com.developmentontheedge.be5.jetty;

import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddedJettyUtils
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private Server jetty;
    private String name = "Be5Jetty";
    private int post = 8200;

    public static void runWebApp() throws Exception
    {
        new EmbeddedJettyUtils().run();

    }

    public final void run()
    {
        Thread.currentThread().setName(name);

        LogConfigurator.configure();

        try
        {
            long startTime = System.currentTimeMillis();
            jetty = new Server(8200);
            jetty.setHandler(getWebAppContext());
            doStart();
            logStarted(startTime);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Unable to start " + name, e);
            System.exit(1);
        }

        try
        {
            jetty.join();
        }
        catch (Exception e)
        {
            log.severe("Interrupted (most likely JVM is shutting down and this is safe to ignore)");
        }
    }

    private void doStart() throws Exception
    {
        String version = this.jetty.getClass().getPackage().getImplementationVersion();
        log.info("Trying to start jetty v" + version);
        this.jetty.start();
        log.info("Started jetty v" + version);
    }

    private void logStarted(long startTime)
    {
        log.info("-------------------------------------------------------");
        log.info("Be5 application running at");
        log.info(" => http://localhost:" + post);
        log.info((System.currentTimeMillis() - startTime) + " ms");
        log.info("-------------------------------------------------------");
    }

    private static WebAppContext getWebAppContext()
    {
        WebAppContext context = new WebAppContext();
        context.setDescriptor("/WEB-INF/web.xml");
        context.setParentLoaderPriority(true);
        setBaseProps(context);
        return context;
    }

    private static void setBaseProps(ServletContextHandler context)
    {
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.setMaxFormContentSize(1024 * 1024 * 1024);
    }
}
