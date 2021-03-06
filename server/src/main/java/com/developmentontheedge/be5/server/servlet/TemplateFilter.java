package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.authentication.UserService;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.server.servlet.support.FilterSupport;
import com.developmentontheedge.be5.server.servlet.support.ServletUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class TemplateFilter extends FilterSupport
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private ServletContext servletContext;
    private TemplateEngine templateEngine;

    private final HtmlMetaTags htmlMetaTags;
    private final UserService userService;
    private final ProjectProvider projectProvider;

    @Inject
    public TemplateFilter(UserService userService, ProjectProvider projectProvider, HtmlMetaTags htmlMetaTags)
    {
        this.userService = userService;
        this.htmlMetaTags = htmlMetaTags;
        this.projectProvider = projectProvider;
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        servletContext = filterConfig.getServletContext();
        this.templateEngine = new TemplateEngine();

        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setCacheable(true);
        templateEngine.setTemplateResolver(templateResolver);

        projectProvider.addToReload(() -> templateEngine.clearTemplateCache());
    }

    @Override
    public void filter(Request req, Response res, FilterChain chain) throws IOException, ServletException
    {
        String message = null;
        try
        {
            if (UserInfoHolder.getLoggedUser() == null)
            {
                userService.initUser(req, res);
            }
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error on initUser", e);
            message = e.getMessage();
        }

        String reqWithoutContext = getRequestWithoutContext(req.getContextPath(), req.getRequestUri());
        if (servletContext.getResourceAsStream("/WEB-INF/templates" + reqWithoutContext + "index.html") == null)
        {
            chain.doFilter(req.getRawRequest(), res.getRawResponse());
        }
        else
        {
            try
            {
                ServletUtils.addHeaders(req.getRawRequest(), res.getRawResponse());
                res.sendHtml(templateEngine.process(reqWithoutContext + "index", getContext(req, message)));
            }
            catch (Throwable e)
            {
                log.log(Level.SEVERE, "Error on precess template", e);
                res.sendHtml(e.getMessage());
            }
        }
    }

    private static String getRequestWithoutContext(String contextPath, String requestUri)
    {
        String reqWithoutContext = requestUri.replaceFirst(contextPath, "");
        if (!reqWithoutContext.endsWith("/")) return reqWithoutContext + "/";
        else return reqWithoutContext;
    }

    private Context getContext(Request req, String message)
    {
        Context context = new Context();
        context.setVariables(htmlMetaTags.getTags(req));
        context.setVariable("requestUrl", req.getRequestUri());
        context.setVariable("contextPath", req.getContextPath());
        context.setVariable("message", message);
        return context;
    }

}
