package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.Be5Caches;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.ConnectionService;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.api.services.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.maven.AppDb;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.util.JULLogger;
import com.developmentontheedge.be5.test.mocks.Be5CachesForTest;
import com.developmentontheedge.be5.test.mocks.CategoriesServiceForTest;
import com.developmentontheedge.be5.test.mocks.ConnectionServiceMock;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.developmentontheedge.be5.test.mocks.DatabaseServiceMock;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import javax.inject.Inject;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.servlet.ServletModule;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.developmentontheedge.be5.test.TestProjectProvider.profileForIntegrationTests;
import static com.developmentontheedge.be5.util.ParseRequestUtils.replaceEmptyStringToNull;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class TestUtils
{
    public static final Logger log = Logger.getLogger(TestUtils.class.getName());

    @Rule
    public ShowCreatedOperations showCreatedOperations = new ShowCreatedOperations();

    @Inject private OperationService operationService;
    @Inject private Meta meta;
    @Inject private OperationExecutor operationExecutor;
    @Inject protected UserAwareMeta userAwareMeta;
    @Inject protected DatabaseModel database;
    @Inject protected SqlService db;

    protected static final String TEST_USER = "testUser";
    protected static final Jsonb jsonb = JsonbBuilder.create();

    @Before
    public void setUpTestUtils()
    {
        if(getInjector() != null)
        {
            getInjector().injectMembers(this);
            initGuest();
        }
    }

    public Injector getInjector()
    {
        return null;
    }

    protected void initUserWithRoles(String... roles)
    {
        TestSession testSession = new TestSession();
        getInjector().getInstance(UserHelper.class).saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles),
                Locale.US, "", testSession);

        UserInfoHolder.setRequest(new TestRequest(testSession));
    }

    protected void initGuest()
    {
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    protected static Injector initInjector(Module... modules)
    {
        return Guice.createInjector(Stage.DEVELOPMENT, modules);
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

    protected Request getSpyMockRequest(String requestUri, Map<String, Object> parameters)
    {
        return getSpyMockRequest(requestUri, parameters, new HashMap<>());
    }

    protected Request getSpyMockRequest(String requestUri, Map<String, Object> parameters, Map<String, Object> sessionValues)
    {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getSession()).thenReturn(mock(HttpSession.class));

        parameters.forEach((k,v) ->
            when(httpServletRequest.getParameter(k)).thenReturn((String) v)
        );

        Request request = Mockito.spy(new RequestImpl(httpServletRequest, null));
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

    public static DynamicPropertySetSupport getDpsS(Map<String, Object> nameValues)
    {
        return getDps(new DynamicPropertySetSupport(), nameValues);
    }

    public static QRec getQRec(Map<String, Object> nameValues)
    {
        return getDps(new QRec(), nameValues);
    }

    public static <T extends DynamicPropertySet> T getDps(T dps, Map<String, Object> nameValues)
    {
        for(Map.Entry<String, Object> entry : nameValues.entrySet())
        {
            dps.add(new DynamicProperty(entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
        return dps;
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName, String operationName,
                                                                          String selectedRows)
    {
        return generateOperation(entityName, queryName, operationName, selectedRows, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName, String operationName,
                                                                          String selectedRows, String values)
    {
        return generateOperation(entityName, queryName, operationName, selectedRows, ParseRequestUtils.getValuesFromJson(values));
    }

    protected Either<Object, OperationResult> generateOperation(String entityName, String queryName, String operationName,
                                                                          String selectedRows, Map<String, Object> presetValues)
    {
        return generateOperation(createOperation(entityName, queryName, operationName, selectedRows), presetValues);
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation, String values)
    {
        return operationService.generate(operation, ParseRequestUtils.getValuesFromJson(values));
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation)
    {
        return operationService.generate(operation, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> generateOperation(Operation operation, Map<String, Object> presetValues)
    {
        return operationService.generate(operation, replaceEmptyStringToNull(presetValues));
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName, String operationName,
                                                                         String selectedRows)
    {
        return executeOperation(entityName, queryName, operationName, selectedRows, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName, String operationName,
                                                                         String selectedRows, String values)
    {
        return executeOperation(entityName, queryName, operationName, selectedRows, ParseRequestUtils.getValuesFromJson(values));
    }

    protected Either<Object, OperationResult> executeOperation(String entityName, String queryName, String operationName,
                                                                         String selectedRows, Map<String, Object> presetValues)
    {
        return executeOperation(createOperation(entityName, queryName, operationName, selectedRows), presetValues);
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation, String values)
    {
        return executeOperation(operation, ParseRequestUtils.getValuesFromJson(values));
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation)
    {
        return executeOperation(operation, Collections.emptyMap());
    }

    protected Either<Object, OperationResult> executeOperation(Operation operation, Map<String, Object> presetValues)
    {
        return operationService.execute(operation, replaceEmptyStringToNull(presetValues));
    }

    protected Operation createOperation(String entityName, String operationName, OperationContext context)
    {
        OperationInfo operationInfo = new OperationInfo(meta.getOperation(entityName, context.getQueryName(), operationName));

        Operation operation = operationExecutor.create(operationInfo, context);
        ShowCreatedOperations.addOperation(operation);

        return operation;
    }

    protected Operation createOperation(String entityName, String queryName, String operationName, String selectedRowsParam)
    {
        OperationInfo operationInfo = new OperationInfo(meta.getOperation(entityName, queryName, operationName));

        String[] stringSelectedRows = ParseRequestUtils.selectedRows(selectedRowsParam);
        Object[] selectedRows = stringSelectedRows;
        if(!operationInfo.getEntityName().startsWith("_"))
        {
            Class<?> primaryKeyColumnType = meta.getColumnType(operationInfo.getEntity(), operationInfo.getPrimaryKey());
            selectedRows = Utils.changeTypes(stringSelectedRows, primaryKeyColumnType);
        }

        Operation operation = operationExecutor.create(operationInfo, new OperationContext(selectedRows, queryName, Collections.emptyMap()));
        ShowCreatedOperations.addOperation(operation);

        return operation;
    }

    protected void setSession(String name, Object value)
    {
        UserInfoHolder.getSession().set(name, value);
    }

    protected Object getSession(String name)
    {
        return UserInfoHolder.getSession().get(name);
    }

    protected Date parseDate(String stringDate)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            return new java.sql.Date(df.parse(stringDate).getTime());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getDpsS(ImmutableMap.of("CODE", tagValue, "Name", tagValue))).collect(Collectors.toList());

        when(SqlServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }

    public static class ShowCreatedOperations extends TestWatcher
    {
        private static List<Operation> operations = Collections.synchronizedList(new ArrayList<>());

        public static void addOperation(Operation operation)
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
            if(!operations.isEmpty())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Created operations:");
                operations.forEach(o ->
                {
                    String line = "\n" + o.getClass().getCanonicalName() + "(" + o.getClass().getSimpleName() + extension(o) + ":0)";
                    sb.append(line);
                });
                log.info(sb.toString());
            }
        }

        private String extension(Operation o)
        {
            if(OPERATION_TYPE_GROOVY.equals(o.getInfo().getModel().getType())){
                return ".groovy";
            } else {
                return ".java";
            }
        }
    }

    protected static void initDb(Injector injector)
    {
        Project project = injector.getInstance(ProjectProvider.class).getProject();

        if(project.getConnectionProfileName() != null &&
                profileForIntegrationTests.equals(project.getConnectionProfileName()))
        {
            try
            {
                log.info(JULLogger.infoBlock("Execute be5:create-db"));
                new AppDb()
                        .setLogger(new JULLogger(log))
                        .setBe5Project(project)
                        .execute();
            }
            catch (MojoFailureException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            log.warning("Fail set '"+ profileForIntegrationTests +"' profile, maybe DatabaseService already initialized." );
        }
    }

    public static class SqlMockModule extends ServletModule
    {
        @Override
        protected void configureServlets()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);

            bind(SqlService.class).to(SqlServiceMock.class).in(Scopes.SINGLETON);
            bind(DatabaseService.class).to(DatabaseServiceMock.class).in(Scopes.SINGLETON);
            bind(ConnectionService.class).to(ConnectionServiceMock.class).in(Scopes.SINGLETON);
            bind(Be5Caches.class).to(Be5CachesForTest.class).in(Scopes.SINGLETON);
        }
    }

    public static class CoreModuleForTest extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(CoreUtils.class).to(CoreUtilsForTest.class).in(Scopes.SINGLETON);
            bind(CategoriesService.class).to(CategoriesServiceForTest.class).in(Scopes.SINGLETON);
        }
    }

    public static class TestProjectProviderModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(ProjectProvider.class).to(TestProjectProvider.class).in(Scopes.SINGLETON);
        }
    }

}
