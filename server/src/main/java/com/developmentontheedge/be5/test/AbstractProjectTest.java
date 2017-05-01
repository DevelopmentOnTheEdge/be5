package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.ComponentProvider;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.impl.MainComponentProvider;
import com.developmentontheedge.be5.api.impl.MainServiceProvider;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.ServerModuleLoader;
import com.developmentontheedge.be5.metadata.model.DdlElement;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Module;
import com.developmentontheedge.be5.metadata.model.TableDef;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractProjectTest
{
    private static final ServerModuleLoader moduleLoader = new ServerModuleLoader();

    protected static final ServiceProvider sp = new MainServiceProvider();
    protected static final ComponentProvider loadedClasses = new MainComponentProvider();

    static {
        try
        {
            moduleLoader.load(sp, loadedClasses);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(sp.getProject().getProject().getConnectionProfile() != null)
        {
            sp.getLoginService().initGuest(null, sp);
            createTables();
            insertTestData();
        }
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

    protected Request getMockRequest(String requestUri){
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri){
        return getSpyMockRequest(requestUri, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters){
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        Mockito.when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected void initUserWithRoles(String... roles)
    {
        sp.getLoginService().saveUser("testUser", Arrays.asList(roles), Locale.US);
    }

}
