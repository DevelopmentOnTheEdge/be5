package com.developmentontheedge.be5;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.ServerModuleLoader;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.model.UserInfo;

import java.io.IOException;
import java.util.Arrays;
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

        createTables();
        insertTestData();
    }

    private static void insertTestData()
    {
        SqlService db = sp.getSqlService();
        db.insert("insert into testtable (name, value) VALUES (?, ?)",
                "test", "1");
        db.insert("insert into testtable (name, value) VALUES (?, ?)",
                "test", "2");
    }

    private static void createTables()
    {
        Module application = sp.getProject().getApplication();
        SqlService db = sp.getSqlService();
        for(Entity entity : application.getOrCreateEntityCollection().getAvailableElements())
        {
            DdlElement scheme = entity.getScheme();
            if(scheme instanceof TableDef)
            {
                final String generatedQuery = scheme.getDdl();
                db.update( generatedQuery );
            }
        }
    }

    protected Request getMockRequestWithUri(String requestUri){
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
