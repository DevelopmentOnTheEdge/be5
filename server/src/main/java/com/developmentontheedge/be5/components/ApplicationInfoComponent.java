package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.AppInfo;
import com.developmentontheedge.be5.Utils;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

public class ApplicationInfoComponent implements Component {
    
    static class ApplicationInfo
    {
        
        final String title;
        final String url;
        
        ApplicationInfo(String title, String url)
        {
            this.title = title;
            this.url = url;
        }
        
    }
	
    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        final AppInfo appInfo;
        
        try
        {
            appInfo = Utils.getAppInfo(req.getRawRequest().getServletContext(), serviceProvider.getDatabaseConnector(), req.getRawRequest());
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }

        if (appInfo.getAppName().equals("com.beanexplorer.be5.servlet"))
        {
            appInfo.setAppName(req.getRawRequest().getContextPath().substring(1));
        }

        res.sendAsRawJson(new ApplicationInfo(appInfo.getAppName(), appInfo.getAppUrl()));
    }
    
}
