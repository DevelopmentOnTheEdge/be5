package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.support.FilterSupport;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;


public class TemplateFilter extends FilterSupport
{
    private ServletContext servletContext;
    private TemplateEngine templateEngine;

    private final UserAwareMeta userAwareMeta;
    private final UserHelper userHelper;

    @Inject
    public TemplateFilter(UserAwareMeta userAwareMeta, UserHelper userHelper)
    {
        this.userAwareMeta = userAwareMeta;
        this.userHelper = userHelper;
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        servletContext = filterConfig.getServletContext();
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
    public void filter(Request req, Response res, FilterChain chain) throws IOException, ServletException
    {
        if (UserInfoHolder.getUserInfo() == null)
        {
            userHelper.initGuest(req);
        }

        String reqWithoutContext = ParseRequestUtils.getRequestWithoutContext(req.getContextPath(), req.getRequestUri());

        if (servletContext.getResourceAsStream("/WEB-INF/templates" + reqWithoutContext + "index.html") == null)
        {
            chain.doFilter(req.getRawRequest(), res.getRawResponse());
            return;
        }

        String title = userAwareMeta.getColumnTitle("index", "page", "title");
        String description = userAwareMeta.getColumnTitle("index", "page", "description");

        Context context = new Context();
        context.setVariable("lang", UserInfoHolder.getLanguage());
        context.setVariable("title", title);
        context.setVariable("description", description);

        context.setVariable("baseUrl", req.getContextPath() + reqWithoutContext);
        context.setVariable("baseUrlWithoutContext", reqWithoutContext);

        res.sendHtml(templateEngine.process(reqWithoutContext + "index", context));
    }

}
