package com.developmentontheedge.be5.components;

import java.lang.reflect.Method;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import javax.servlet.ServletContext;
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
            appInfo = getApplicationInfo(req);
            res.sendAsRawJson(appInfo);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    
    public static ApplicationInfo getApplicationInfo(Request req)throws Exception
    {
    	// TODO
    	//String appName = getSystemSetting( connector, DatabaseConstants.APPLICATION_NAME );
        //String appUrl = getSystemSetting( connector, DatabaseConstants.APPLICATION_URL );

    	String title = "";

    	ServletContext context = req.getRawRequest().getServletContext();
        if( context != null )
        {
            try
            {
                Method ctxNameMethod = context.getClass().getMethod( "getServletContextName", ( Class[] )null );
                if( ctxNameMethod != null )
                {
                    title = (String)ctxNameMethod.invoke(context, (Object[])null);
                }
            }
            catch( NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e )
            {} 

            if( "com.developmentontheedge.be5.servlet".equals(title) )
            	title = req.getRawRequest().getContextPath().substring(1);
        }

        // TODO - localise title 

        HttpServletRequest request = req.getRawRequest();
        String url = "";
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
    public static String getContextPrefix( HttpServletRequest request )
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
