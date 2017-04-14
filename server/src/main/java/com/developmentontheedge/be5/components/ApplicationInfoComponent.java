package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;

import javax.servlet.http.HttpServletRequest;

/**
 * ApplicationInfoComponent returns ApplicationInfo(title, url) for current application (servlet context).
 */
public class ApplicationInfoComponent implements Component 
{
    public static class ApplicationInfo
    {
       
        ApplicationInfo(String title, String url)
        {
            this.title = title;
            this.url = url;
        }

        private String title;
        public String getTitle()
        {
        	return title;
        }
        
        final String url;
        public String getURL()
        {
        	return url;
        }
        
    }
	
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        final ApplicationInfo appInfo;
        
        try
        {
            appInfo = getApplicationInfo(req, serviceProvider);
            res.sendAsRawJson(appInfo);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    
    public static ApplicationInfo getApplicationInfo(Request req, ServiceProvider serviceProvider)throws Exception
    {
        String title = UserAwareMeta.get(serviceProvider)
                .getColumnTitle("index.jsp", "application", "applicationName")
                .orElse("Be5 Application");

        String url = "";
        HttpServletRequest request = req.getRawRequest();
        if( request != null )
        {
            url = getContextPrefix( request );
        }

        return new ApplicationInfo(title, url);
    }

    /**
     * Getting link from the request regardless of it's parameters
     *
     * @param request
     * @return link
     */
    private static String getContextPrefix( HttpServletRequest request )
    {
        String ret;
        if( request.isSecure() )
        {
            ret = "https://";
        }
        else
        {
            ret = "http://";
        }

        ret += request.getServerName();

        int port = request.getServerPort();

        if( request.isSecure() && port != 443 && port != 0 )
        {
            ret += ":" + port;
        }
        else if( !request.isSecure() && port != 80 && port != 0 )
        {
            ret += ":" + port;
        }

        ret += request.getContextPath() + "/";

        return ret;
    }
    
}
