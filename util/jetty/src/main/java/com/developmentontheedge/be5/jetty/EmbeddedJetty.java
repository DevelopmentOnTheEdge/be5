package com.developmentontheedge.be5.jetty;

import com.developmentontheedge.be5.logging.LogConfigurator;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
//import org.eclipse.jetty.server.handler.GzipHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddedJetty
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private String resourceBase = "src/main/webapp";
    private String descriptorPath = "/WEB-INF/web.xml";
    private int port = 8200;
    private String resourceFolder = "files";
    private Server jetty;

    public void run()
    {
        long startTime = System.currentTimeMillis();
        LogConfigurator.configure();
        checkDescriptor();
        Thread.currentThread().setName("main");

        try
        {
            jetty = new Server(port);
            jetty.setHandler(getHandlers());
            jetty.start();
            logStarted(startTime);
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

    private HandlerCollection getHandlers()
    {
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(getRequestLogHandler());
        handlers.addHandler(getResourceHandler());
        handlers.addHandler(getGzipHandler(getWebAppContext()));
        return handlers;
    }

    private void logStarted(long startTime)
    {
        long time = System.currentTimeMillis() - startTime;
        log.info("Jetty started on http://localhost:" + port + " - " + time + " ms");
    }

    private WebAppContext getWebAppContext()
    {
        WebAppContext context = new WebAppContext();
        context.setSessionHandler(getSessionHandler());
        context.setDescriptor(descriptorPath);
        context.setParentLoaderPriority(true);
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }
        context.setContextPath("/");
        context.setResourceBase(resourceBase);
        context.setMaxFormContentSize(1024 * 1024 * 1024);
        context.setDefaultsDescriptor(null);
        context.addServlet(DefaultServlet.class, "/");
        log.info(context.toString());
        return context;
    }

    private GzipHandler getGzipHandler(WebAppContext webAppContext)
    {
        GzipHandler gzipHandler = new GzipHandler();
        //gzipHandler.setMimeTypes(new HashSet<>(Arrays.asList("text/html", "text/plain", "text/xml",
        //        "text/css", "application/javascript", "application/x-javascript", "text/javascript")));
        gzipHandler.addIncludedMimeTypes("text/html", "text/plain", "text/xml",
                "text/css", "application/javascript", "application/x-javascript", "text/javascript");
        gzipHandler.setHandler(webAppContext);
        return gzipHandler;
    }

    private SessionHandler getSessionHandler()
    {
        HashSessionManager sessionManager = new HashSessionManager();
        try
        {
            File file = new File("./target/sessions");
            file.mkdirs();
            sessionManager.setStoreDirectory(file);
        }
        catch (IOException e)
        {
            log.log(Level.SEVERE, "", e);
        }
        sessionManager.setSessionIdManager(new HashSessionIdManager());
        sessionManager.setSavePeriod(60);
        sessionManager.setMaxInactiveInterval(-1);
        sessionManager.setDeleteUnrestorableSessions(true);

        return new SessionHandler(sessionManager);
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

    private RequestLogHandler getRequestLogHandler()
    {
        NCSARequestLog requestLog = new NCSARequestLog();
        File file = new File("./target/access_logs");
        file.mkdirs();
        requestLog.setFilename(file.getAbsolutePath() + "/yyyy_mm_dd.request.log");
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(false);
        requestLog.setLogTimeZone("GMT");
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        return requestLogHandler;
    }

    private ContextHandler getResourceHandler()
    {
        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/files");
        File dir0 = Paths.get("./" + resourceFolder).toFile();
        //try
        //{
            context0.setBaseResource(Resource.newResource(dir0));
        //}
        //catch (IOException e)
        //{
        //    e.printStackTrace();
        //}
        ResourceHandler rh0 = new ResourceHandler();
        context0.setHandler(rh0);
        return context0;
    }

    public EmbeddedJetty setPort(int port)
    {
        this.port = port;
        return this;
    }

    public EmbeddedJetty setResourceFolder(String resourceFolder)
    {
        this.resourceFolder = resourceFolder;
        return this;
    }
}
