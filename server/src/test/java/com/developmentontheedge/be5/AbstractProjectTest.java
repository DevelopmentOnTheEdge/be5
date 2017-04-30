package com.developmentontheedge.be5;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.env.ServerModuleLoader;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.UserInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractProjectTest
{
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();

    protected static final ServiceProvider sp = new MainServiceProvider();
    protected static final ComponentProvider loadedClasses = new MainComponentProvider();

    static {
        try
        {
            moduleLoader.load(sp, loadedClasses);
            sp.getLoginService().initGuest(null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected Request getRequestWithUri(String requestUri){
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected void initUserWithRoles(String... roles)
    {
        UserInfo ui = new UserInfo();
        ui.setUserName("testUser");
        ui.setLocale(Locale.US);

        ui.setCurrentRoles(Arrays.asList(roles));
        ui.setAvailableRoles(Arrays.asList(roles));

        UserInfoHolder.setUserInfo(ui);
    }

}
