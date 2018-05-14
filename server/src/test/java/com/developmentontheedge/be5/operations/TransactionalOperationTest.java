package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import com.developmentontheedge.be5.util.Either;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TransactionalOperationTest extends ServerBe5ProjectDBTest
{
    @Before
    public void beforeSqlMockOperationTest()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void test()
    {
        Either<Object, OperationResult> result = executeOperation(
                "testtableAdmin", "All records", "TestTransactionalOperation", "0",
                        "{'name':'test', 'value': 5}");

        assertEquals("{'details':'table/testtableAdmin/All records','status':'redirect'}",
                oneQuotes(jsonb.toJson(result.getSecond())));
    }

    @Test
    public void testNullValues()
    {
        Either<Object, OperationResult> result = generateOperation(
                "testtableAdmin", "All records", "TestTransactionalOperation", "0",
                        "{'nullValues':'yes'}");

        assertEquals("{'details':'table/testtableAdmin/All records','status':'redirect'}",
                oneQuotes(jsonb.toJson(result.getSecond())));
    }


}