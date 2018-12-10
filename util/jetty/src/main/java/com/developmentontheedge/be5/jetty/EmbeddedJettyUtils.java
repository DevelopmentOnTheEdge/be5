package com.developmentontheedge.be5.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedJettyUtils
{
    public static void runWebApp() throws Exception
    {
        Server server = new Server(8200);
        server.setHandler(getWebAppContext());
        server.start();
        server.join();
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
