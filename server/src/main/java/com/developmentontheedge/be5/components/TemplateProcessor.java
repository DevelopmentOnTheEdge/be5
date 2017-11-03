package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.env.Injector;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;


public class TemplateProcessor implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        // This prevents triggering engine executions for resource URLs
        if (req.getRequestUri().startsWith("/static"))
        {
            return;
        }
        processTemplate(req, res, injector);
    }

    public void processTemplate(Request req, Response res, Injector injector)
    {
        Context context = new Context();
        context.setVariable("title", "Name");
        context.setVariable("url", "http://url");


        res.sendHtml(getHtmlTemplateEngine().process("index", context));
    }

    public TemplateEngine getHtmlTemplateEngine()
    {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(textTemplateResolver());
        return templateEngine;
    }

    public ITemplateResolver textTemplateResolver()
    {
        //getClass().getClassLoader().getResource("manager/index.html")
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        //templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF8");
//        templateResolver.setCheckExistence(true);
//        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
