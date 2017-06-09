package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Binder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.YamlBinder;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OperationsTest extends AbstractProjectTest{

    private Injector sqlMockInjector = Be5.createInjector(new SqlMockBinder());
    private OperationService operationService = sqlMockInjector.get(OperationService.class);

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void deleteOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Delete", "1", "");

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationService.generate(req).getSecond());

        verify(SqlServiceMock.sqlServiceMock).update("delete from testtableAdmin where id = ?", 1L);
    }

    @Test
    public void testOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
                new Gson().toJson(ImmutableMap.of("name","testName","number", "1")));

        Either<FormPresentation, OperationResult> generate = operationService.generate(req);

        FormPresentation form = generate.getFirst();

        assertEquals("TestOperation", form.title);

        assertEquals("{'name':'testName','number':1}",
                oneQuotes(form.bean.getJsonObject("values").toString()));

        assertEquals("Name",
                form.bean.getJsonObject("meta").getJsonObject("/name").getString("displayName"));

        assertEquals("Number",
                form.bean.getJsonObject("meta").getJsonObject("/number").getString("displayName"));

        assertEquals("['/name','/number']",
                oneQuotes(form.bean.getJsonArray("order").toString()));

        OperationResult result = operationService.execute(req);
        assertEquals(OperationResult.redirect("table/testtableAdmin/All records/number=1/name=testName"), result);


    }

    @Test
    public void testOperationParameters(){
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0","{}"));

        assertEquals("{'name':'','number':0}", oneQuotes(generate.getFirst().getBean().getJsonObject("values").toString()));

//todo check error and add error msg
//        Either<FormPresentation, OperationResult> generate1 = operationService.generate(
//                getSpyMockRecForOp("testtableAdmin", "All records", "TestOperation", "0",
//                        doubleQuotes("{'name':'testName','number':'a'}")));
//
//        assertEquals("{'name':'testName','number':0}", oneQuotes(generate1.getFirst().getBean().getJsonObject("values").toString()));


    }


    public static class SqlServiceMock implements SqlService{

        public static SqlService sqlServiceMock = mock(SqlService.class);

        @Override
        public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params)
        {
            return sqlServiceMock.query(sql, rsh, params);
        }

        @Override
        public <T> T select(String sql, ResultSetParser<T> parser, Object... params)
        {
            return sqlServiceMock.select(sql, parser, params);
        }

        @Override
        public <T> List<T> selectList(String sql, ResultSetParser<T> parser, Object... params)
        {
            return sqlServiceMock.selectList(sql, parser, params);
        }

        @Override
        public <T> T getScalar(String sql, Object... params)
        {
            return sqlServiceMock.getScalar(sql, params);
        }

        @Override
        public int update(String sql, Object... params)
        {
            return sqlServiceMock.update(sql, params);
        }

        @Override
        public <T> T insert(String sql, Object... params)
        {
            return sqlServiceMock.insert(sql, params);
        }
    }

    class SqlMockBinder implements Binder{

        @Override
        public void configure(Map<String, Class<?>> loadedClasses, Map<Class<?>, Class<?>> bindings, Map<Class<?>, Object> configurations)
        {
            new YamlBinder().configure(loadedClasses, bindings, configurations);
            bindings.put(SqlService.class, SqlServiceMock.class);
        }
    }
}