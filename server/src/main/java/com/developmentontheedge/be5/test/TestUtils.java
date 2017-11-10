package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class TestUtils
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    static final String profileForIntegrationTests = "profileForIntegrationTests";

    static Injector initInjector(Binder binder)
    {
        Injector injector = Be5.createInjector(binder);
        Project project = injector.getProject();
        initProfile(project);

        injector.get(LoginService.class).initGuest(null);
        return injector;
    }

    private static void initProfile(Project project)
    {
        if(project.getConnectionProfile() == null || !profileForIntegrationTests.equals(project.getConnectionProfile().getName()))
        {
            ProjectTestUtils.createH2Profile(project, profileForIntegrationTests);
            project.setConnectionProfileName(profileForIntegrationTests);
        }
    }

    protected static String oneQuotes(Object s)
    {
        return s.toString().replace("\"", "'");
    }

    protected static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

    protected Request getMockRequest(String requestUri)
    {
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri)
    {
        return getSpyMockRequest(requestUri, new HashMap<>(), new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters)
    {
        return getSpyMockRequest(requestUri, parameters, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, String> parameters, Map<String, Object> sessionValues)
    {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null, parameters));
        when(request.getRequestUri()).thenReturn(requestUri);

        for (Map.Entry<String, Object> entry: sessionValues.entrySet())
        {
            when(request.getAttribute(entry.getKey())).thenReturn(entry.getValue());
        }

        return request;
    }

    protected Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values, Map<String, Object> sessionValues)
    {
        return getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, entity,
                RestApiConstants.QUERY, query,
                RestApiConstants.OPERATION, operation,
                RestApiConstants.SELECTED_ROWS, selectedRows,
                RestApiConstants.VALUES, values),
                sessionValues
        );
    }

    protected Request getSpyMockRecForQuery(String entity, String query, String values)
    {
        return getSpyMockRecForQuery(entity, query, values, new HashMap<>());
    }

    protected Request getSpyMockRecForQuery(String entity, String query, String values, Map<String, Object> sessionValues)
    {
        return getSpyMockRequest("", ImmutableMap.of(
                RestApiConstants.ENTITY, entity,
                RestApiConstants.QUERY, query,
                RestApiConstants.VALUES, values),
                sessionValues
        );
    }

    protected Request getSpyMockRecForOp(String entity, String query, String operation, String selectedRows, String values)
    {
        return getSpyMockRecForOp(entity, query, operation, selectedRows, values, new HashMap<>());
    }

    public static String resultSetToString(ResultSet rs) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                if(rs.getObject(i) != null)
                    list.add(rs.getObject(i).toString());
                else{
                    list.add("null");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.stream().collect(Collectors.joining(","));
    }

    public static DynamicPropertySet getDps(Map<String, Object> nameValues)
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        for(Map.Entry<String, Object> entry : nameValues.entrySet()){
            dps.add(new DynamicProperty(entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
        return dps;
    }

}
