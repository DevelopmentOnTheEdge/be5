package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.mail.MailService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.services.users.UserHelper;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.test.mocks.OperationLoggingMock;
import com.developmentontheedge.be5.test.mocks.TestMailService;
import com.developmentontheedge.be5.test.mocks.TestQuerySession;
import com.developmentontheedge.be5.test.mocks.TestRequest;
import com.developmentontheedge.be5.test.mocks.TestResponse;
import com.developmentontheedge.be5.test.mocks.TestSession;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.developmentontheedge.be5.operation.util.OperationUtils.replaceEmptyStringToNull;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class TestUtils extends BaseTest
{
    @Rule
    public ShowCreatedOperations showCreatedOperations = new ShowCreatedOperations();

    @Inject
    private OperationService operationService;
    @Inject
    private OperationExecutor operationExecutor;
    @Inject
    protected DatabaseModel database;
    @Inject
    protected Session session;

    @Before
    public void setUpTestUtils()
    {
        initGuest();
    }

    protected void initUserWithRoles(String... roles)
    {
        getInjector().getInstance(UserHelper.class).
                saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles), Locale.US, "", false);
    }

    protected void initUserWithNameAndRoles(String name, String... roles)
    {
        getInjector().getInstance(UserHelper.class).
                saveUser(name, Arrays.asList(roles), Arrays.asList(roles), Locale.US, "", false);
    }

    protected void initGuest()
    {
        initUserWithNameAndRoles(RoleType.ROLE_GUEST, RoleType.ROLE_GUEST);
    }

    protected Request getMockRequest(String requestUri)
    {
        Request request = mock(Request.class);
        when(request.getRequestUri()).thenReturn(requestUri);
        return request;
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, Object> parameters)
    {
        return getSpyMockRequest(requestUri, parameters, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, Object> parameters,
                                        Map<String, Object> sessionValues)
    {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        parameters.forEach((k, v) ->
                when(httpServletRequest.getParameter(k)).thenReturn((String) v)
        );

        Request request = Mockito.spy(new RequestImpl(httpServletRequest));
        when(request.getRequestUri()).thenReturn(requestUri);

        for (Map.Entry<String, Object> entry : sessionValues.entrySet())
        {
            when(request.getAttribute(entry.getKey())).thenReturn(entry.getValue());
        }

        return request;
    }

    protected static QRec getQRec(Map<String, ?> nameValues)
    {
        return getDps(new QRec(), nameValues);
    }

    protected static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getQRec(ImmutableMap.of("CODE", tagValue, "Name", tagValue)))
                .collect(Collectors.toList());

        when(DbServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName,
                                                    String operationName, String selectedRows)
    {
        return generateOperation(entityName, queryName, operationName, selectedRows, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName,
                                                    String operationName, String selectedRows, String values)
    {
        return generateOperation(entityName, queryName, operationName, selectedRows,
                parseValues(values));
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName,
                                    String operationName, String selectedRows, Map<String, Object> presetValues)
    {
        return generateOperation(createOperation(entityName, queryName, operationName, selectedRows), presetValues);
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation, String values)
    {
        return operationService.generate(operation, parseValues(values));
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation)
    {
        return operationService.generate(operation, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation, Map<String, Object> presetValues)
    {
        return operationService.generate(operation, replaceEmptyStringToNull(presetValues));
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName,
                                       String operationName, String selectedRows)
    {
        return executeOperation(entityName, queryName, operationName, selectedRows, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName,
                                       String operationName, String selectedRows, String values)
    {
        return executeOperation(entityName, queryName, operationName, selectedRows,
                parseValues(values));
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName,
                                       String operationName, String selectedRows, Map<String, Object> presetValues)
    {
        return executeOperation(createOperation(entityName, queryName, operationName, selectedRows), presetValues);
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation, String values)
    {
        return executeOperation(operation, parseValues(values));
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation)
    {
        return executeOperation(operation, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation, Map<String, Object> presetValues)
    {
        return operationService.execute(operation, replaceEmptyStringToNull(presetValues));
    }

    protected Operation createOperation(String entityName, String queryName, String operationName, String selectedRows)
    {
        Map<String, Object> params;
        if (Utils.isEmpty(selectedRows))
        {
            params = Collections.emptyMap();
        }
        else
        {
            params = Collections.singletonMap(OperationConstants.SELECTED_ROWS, parseSelectedRows(selectedRows));
        }
        return createOperation(entityName, queryName, operationName, params);
    }

    private static String[] parseSelectedRows(String selectedRowsString)
    {
        if (selectedRowsString == null || selectedRowsString.trim().isEmpty()) return new String[0];
        return selectedRowsString.split(",");
    }

    protected Operation createOperation(String entityName, String queryName, String operationName,
                                        Map<String, ?> operationParams)
    {
        OperationInfo operationInfo = new OperationInfo(meta.getOperation(entityName, queryName, operationName));

        OperationContext operationContext = operationExecutor.getOperationContext(
                operationInfo, queryName, operationParams);

        Operation operation = operationExecutor.create(operationInfo, operationContext);
        ShowCreatedOperations.addOperation(operation);

        return operation;
    }

    public static class ShowCreatedOperations extends TestWatcher
    {
        private static List<Operation> operations = Collections.synchronizedList(new ArrayList<>());

        static void addOperation(Operation operation)
        {
            operations.add(operation);
        }

        @Override
        protected void starting(Description description)
        {
            operations.clear();
        }

        @Override
        protected void failed(Throwable e, Description description)
        {
            if (!operations.isEmpty())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Created operations:");
                operations.forEach(o ->
                {
                    String line = "\n" + o.getClass().getCanonicalName() +
                            "(" + o.getClass().getSimpleName() + extension(o) + ":0)";
                    sb.append(line);
                });
                log.info(sb.toString());
            }
        }

        private String extension(Operation o)
        {
            if (OPERATION_TYPE_GROOVY.equals(o.getInfo().getModel().getType()))
            {
                return ".groovy";
            }
            else
            {
                return ".java";
            }
        }
    }

    public static class DbMockTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbMockTestModule());
            install(new WebTestModule());
            bind(MailService.class).to(TestMailService.class).in(Scopes.SINGLETON);
            bind(OperationLogging.class).to(OperationLoggingMock.class).in(Scopes.SINGLETON);
        }
    }

    public static class DbTestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(new BaseDbTestModule());
            install(new WebTestModule());
            bind(MailService.class).to(TestMailService.class).in(Scopes.SINGLETON);
            bind(OperationLogging.class).to(OperationLoggingMock.class).in(Scopes.SINGLETON);
        }
    }

    public static class WebTestModule extends ServletModule
    {
        @Override
        protected void configureServlets()
        {
            bind(Session.class).to(TestSession.class).in(Scopes.SINGLETON);
            bind(QuerySession.class).to(TestQuerySession.class).in(Scopes.SINGLETON);
            bind(Request.class).to(TestRequest.class).in(Scopes.SINGLETON);
            bind(Response.class).to(TestResponse.class).in(Scopes.SINGLETON);
        }
    }

    public static Map<String, Object> parseValues(String json)
    {
        if (json != null && !json.isEmpty())
        {
            Map<String, String> o = JsonFactory.jsonb.fromJson(json, new HashMap<String, String>()
            {
            }.getClass().getGenericSuperclass());
            return new HashMap<>(o);
        }
        else
        {
            return new HashMap<>();
        }
    }
}
