package com.developmentontheedge.be5.jetty;

import com.developmentontheedge.be5.base.services.impl.LogConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddedJetty
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private String resourceBase = "src/main/webapp";
    private String descriptorPath = "/WEB-INF/web.xml";
    private int port = 8200;
    private Server jetty;

    public void run()
    {
        LogConfigurator.configure();
        checkDescriptor();

        try
        {
            long startTime = System.currentTimeMillis();
            jetty = new Server(port);
            WebAppContext webAppContext = getWebAppContext();
            jetty.setHandler(webAppContext);
            doStart();
            logStarted(webAppContext, startTime);
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE, "Unable to start jetty", e);
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
        String version = jetty.getClass().getPackage().getImplementationVersion();
        log.info("Trying to start jetty v" + version);
        jetty.start();
    }

    private void logStarted(WebAppContext webAppContext, long startTime)
    {
        log.info("-------------------------------------------------------");
        log.info(webAppContext.toString());
        log.info((System.currentTimeMillis() - startTime) + " ms");
        log.info("Started Jetty Server");
        log.info(" => http://localhost:" + port);
        log.info("-------------------------------------------------------");
    }

    private WebAppContext getWebAppContext()
    {
        WebAppContext context = new WebAppContext();
        context.setDescriptor(descriptorPath);
        context.setParentLoaderPriority(true);
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }
        context.setContextPath("/");
        context.setResourceBase(resourceBase);
        context.setMaxFormContentSize(1024 * 1024 * 1024);
        return context;
    }

    private void checkDescriptor()
    {
        File descriptor = new File(resourceBase + descriptorPath);
        if (!descriptor.exists())
        {
            log.severe("The file " + descriptor.getAbsolutePath() + " does not exists.\n" +
                    "Please set the correct working directory. Current: " + new File("").getAbsolutePath());
            System.exit(1);
        }
    }

    public EmbeddedJetty setPort(int port)
    {
        this.port = port;
        return this;
    }
}
