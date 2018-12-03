package com.developmentontheedge.be5.jetty;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.DispatcherType;
import java.io.File;
import java.util.EnumSet;
import java.util.EventListener;

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class EmbeddedJettyUtils
{
    public static void run(EventListener eventListener) throws Exception
    {
        Server server = new Server(8200);
        ServletContextHandler context = new ServletContextHandler(server, "/", SESSIONS);
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        }

        context.setBaseResource(Resource.newResource(new File("src/main/webapp")));
        context.addEventListener(eventListener);
        context.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        context.addServlet(DefaultServlet.class, "/");
        server.start();
    }
}
