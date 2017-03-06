package com.developmentontheedge.be5.legacy;

import javax.servlet.http.HttpServletRequest;

import com.developmentontheedge.be5.UserInfo;
import com.developmentontheedge.be5.api.ServiceProvider;

public class LegacyOperationsService
{

    private final ServiceProvider serviceProvider;

    public LegacyOperationsService(ServiceProvider serviceProvider)
    {
        this.serviceProvider = serviceProvider;
    }
    
    public LegacyOperationFactory createFactory(UserInfo user, HttpServletRequest request)
    {
        return new LegacyOperationFactory(serviceProvider, user, request);
    }
    
}
