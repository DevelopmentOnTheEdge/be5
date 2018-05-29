package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * ApplicationInfoComponent returns ApplicationInfo(title, url) for current application (servlet context).
 */
public class ApplicationInfoController extends ControllerSupport implements Controller
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

    private final UserAwareMeta userAwareMeta;

    @Inject
    public ApplicationInfoController(UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public void generate(Request req, Response res)
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

    public ApplicationInfo getApplicationInfo(Request req)
    {
        String title = userAwareMeta
                .getColumnTitle("index", "page", "title");

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
