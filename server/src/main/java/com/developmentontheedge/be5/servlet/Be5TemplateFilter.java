package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.impl.ResponseImpl;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class Be5TemplateFilter implements Filter
{
    private static final Logger log = Logger.getLogger(Be5TemplateFilter.class.getName());

    private ServletContext servletContext;
    private TemplateEngine templateEngine;

    //TODO private final DaemonStarter starter;

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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;

        String reqWithoutContext = ParseRequestUtils.getRequestWithoutContext(req.getContextPath(), req.getRequestURI());

        if (servletContext.getResourceAsStream("/WEB-INF/templates" + reqWithoutContext + "index.html") == null)
        {
            chain.doFilter(request, response);
            return;
        }

//        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);
//        String title = userAwareMeta.getColumnTitle("index", "page", "title");
//        String description = userAwareMeta.getColumnTitle("index", "page", "description");
        String title = "123";
        String description = "12345";


        Context context = new Context();
        context.setVariable("lang", UserInfoHolder.getLanguage());
        context.setVariable("title", title);
        context.setVariable("description", description);

        context.setVariable("baseUrl", req.getContextPath() + reqWithoutContext);
        context.setVariable("baseUrlWithoutContext", reqWithoutContext);

        Response res = getResponse((HttpServletRequest) request, (HttpServletResponse) response);
        res.sendHtml(templateEngine.process(reqWithoutContext + "index", context));
    }

    private Response getResponse(HttpServletRequest request, HttpServletResponse response)
    {
        String origin = request.getHeader("Origin");// TODO test origin

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Methods", "POST, GET");
        response.addHeader("Access-Control-Max-Age", "1728000");

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        return new ResponseImpl(response);
    }

    @Override
    public void destroy()
    {
        // nothing to do
    }

}
