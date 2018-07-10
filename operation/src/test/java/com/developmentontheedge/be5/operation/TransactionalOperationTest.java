package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TransactionalOperationTest extends OperationBe5ProjectDBTest
{
    @Before
    public void beforeSqlMockOperationTest()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void test()
    {
        Either<Object, OperationResult> result = executeOperation(
                "testtableAdmin", "All records", "TestTransactionalOperation", "0",
                ImmutableMap.of("name", "test", "value", 5));

        Assert.assertEquals("{'details':'table/testtableAdmin/All records','status':'redirect'}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(result.getSecond())));
    }

    @Test
    public void testNullValues()
    {
        Either<Object, OperationResult> result = generateOperation(
                "testtableAdmin", "All records", "TestTransactionalOperation", "0",
                ImmutableMap.of("nullValues", "yes"));

        Assert.assertEquals("{'details':'table/testtableAdmin/All records','status':'redirect'}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(result.getSecond())));
    }


}