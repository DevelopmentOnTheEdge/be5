package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.inject.Injector;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;


public class TemplateProcessor implements Component
{
    private final TemplateEngine templateEngine;

    public TemplateProcessor(ServletContext servletContext)
    {
        this.templateEngine = new TemplateEngine();

        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);

        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
        templateResolver.setCacheTTLMs(3600000L);

        // Cache is set to true by default. Set to false if you want templates to
        // be automatically updated when modified.
        templateResolver.setCacheable(true);

        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);
        String title = userAwareMeta.getColumnTitle("index", "page", "title");
        String description = userAwareMeta.getColumnTitle("index", "page", "description");

        Context context = new Context();
        context.setVariable("lang", UserInfoHolder.getLanguage());
        context.setVariable("title", title);
        context.setVariable("description", description);

        String reqWithoutContext = req.getRequestUri().replaceFirst(req.getContextPath(), "");
        if(!reqWithoutContext.endsWith("/"))reqWithoutContext += "/";

        context.setVariable("baseUrl", req.getContextPath() + reqWithoutContext);
        context.setVariable("baseUrlWithoutContext", reqWithoutContext);

        res.sendHtml(templateEngine.process(reqWithoutContext + "index", context));
    }

}
