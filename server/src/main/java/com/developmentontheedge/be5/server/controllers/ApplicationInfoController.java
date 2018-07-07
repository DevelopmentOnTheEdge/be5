package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;


public class ApplicationInfoController extends JsonApiController
{
    public static class ApplicationInfo
    {
        private String title;

        ApplicationInfo(String title)
        {
            this.title = title;
        }

        public String getTitle()
        {
            return title;
        }
    }

    private final UserAwareMeta userAwareMeta;

    @Inject
    public ApplicationInfoController(UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public Object generate(Request req, String requestSubUrl)
    {
        try
        {
            return getApplicationInfo();
        } catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    public ApplicationInfo getApplicationInfo()
    {
        String title = userAwareMeta
                .getColumnTitle("index", "page", "title");

        return new ApplicationInfo(title);
    }

//    /**
//     * Getting link from the request regardless of it's parameters
//     *
//     * @param request
//     * @return link
//     */
//    private static String getContextPrefix( HttpServletRequest request )
//    {
//        String ret;
//        if( request.isSecure() )
//        {
//            ret = "https://";
//        }
//        else
//        {
//            ret = "http://";
//        }
//
//        ret += request.getServerName();
//
//        int port = request.getServerPort();
//
//        if( request.isSecure() && port != 443 && port != 0 )
//        {
//            ret += ":" + port;
//        }
//        else if( !request.isSecure() && port != 80 && port != 0 )
//        {
//            ret += ":" + port;
//        }
//
//        ret += request.getContextPath() + "/";
//
//        return ret;
//    }

}
