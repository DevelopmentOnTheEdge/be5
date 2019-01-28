package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.be5.web.Response;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class QueryBuilderTest extends SqlMockOperationTest
{
    @Inject
    private QueryBuilderController component;

    @Test
    public void generate()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("/api/queryBuilder/", ImmutableMap.of(
                "sql", "select * from testtable limit 1",
                RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())), response);

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void editor()
    {
        JsonApiModel data = component.getEditorData();
        Map<String, Object> objectMap = (Map<String, Object>) data.getData().getAttributes();
        List<String> functions = (List<String>) objectMap.get("functions");
        List<String> tableNames = (List<String>) objectMap.get("tableNames");
        assertTrue(functions.contains("concat"));
        assertTrue(tableNames.contains("testtableAdmin"));
    }

    @Test
    public void insert()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("/api/queryBuilder/", ImmutableMap.of(
                "sql", "insert into testtable(name, value) VALUES('a', 'b')",
                RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())), response);

        verify(DbServiceMock.mock).insert("insert into testtable(name, value) VALUES('a', 'b')");

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void update()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("/api/queryBuilder/", ImmutableMap.of(
                "sql", "update testtable SET name = 'test' WHERE id = 1",
                RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())), response);

        verify(DbServiceMock.mock).update("update testtable SET name = 'test' WHERE id = 1");

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

    @Test
    public void updateWithoutBeSql()
    {
        Response response = mock(Response.class);
        component.generate(getSpyMockRequest("/api/queryBuilder/", ImmutableMap.of(
                "sql", "update testtable SET name = 'test' WHERE id = 1",
                "updateWithoutBeSql", "true",
                RestApiConstants.TIMESTAMP_PARAM, "" + System.currentTimeMillis())), response);

        verify(DbServiceMock.mock).update("update testtable SET name = 'test' WHERE id = 1");

        verify(response).sendAsJson(any(JsonApiModel.class));
    }

}
