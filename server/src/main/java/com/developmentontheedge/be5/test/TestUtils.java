package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.api.impl.RequestImpl;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.model.QRec;
import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.test.mocks.CategoriesServiceForTest;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.be5.base.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.model.Operation.OPERATION_TYPE_GROOVY;
import static com.developmentontheedge.be5.util.ParseRequestUtils.replaceEmptyStringToNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class TestUtils extends BaseTestUtils
{
    public static final Logger log = Logger.getLogger(BaseTestUtils.class.getName());

    @Rule
    public ShowCreatedOperations showCreatedOperations = new ShowCreatedOperations();

    @Inject private OperationService operationService;
    @Inject private Meta meta;
    @Inject private OperationExecutor operationExecutor;
    @Inject protected UserAwareMeta userAwareMeta;
    @Inject protected DatabaseModel database;
    @Inject protected DbService db;

    protected static final Jsonb jsonb = JsonbBuilder.create();

    protected void initUserWithRoles(String... roles)
    {
        TestSession testSession = new TestSession();
        UserInfo userInfo = getInjector().getInstance(UserHelper.class).saveUser(TEST_USER, Arrays.asList(roles), Arrays.asList(roles),
                Locale.US, "", testSession);

        UserInfoHolder.setRequest(new TestRequest(testSession));
        UserInfoProviderForTest.userInfo = userInfo;
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

    public static QRec getQRec(Map<String, Object> nameValues)
    {
        return getDps(new QRec(), nameValues);
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

    public static class CoreModuleForTest extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(CoreUtils.class).to(CoreUtilsForTest.class).in(Scopes.SINGLETON);
            bind(CategoriesService.class).to(CategoriesServiceForTest.class).in(Scopes.SINGLETON);
        }
    }

}
