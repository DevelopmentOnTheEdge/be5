package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.util.Either;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CustomOperationTest extends SqlMockOperationTest
{
    @Test
    public void getParametersTest()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "CustomOperation", "0",
                        "{}"));

        oneAssert(generate);
    }

    @Test
    public void getParametersReload()
    {
        Either<FormPresentation, OperationResult> generate = operationService.generate(
                getSpyMockRecForOp("testtableAdmin", "All records", "CustomOperation", "0",
                        "{'name':'','value':'2'}"));

        oneAssert(generate);
    }

    void oneAssert(Either<FormPresentation, OperationResult> generate){
        assertEquals("{'values':{'name':'','value':'4'},'meta':{'/name':{'displayName':'name'},'/value':{'displayName':'value','type':'Integer','readOnly':true,'canBeNull':true}},'order':['/name','/value']}",
                oneQuotes(generate.getFirst().getBean().toString()));
    }
}