package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.beans.json.JsonFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CustomOperationTest extends SqlMockOperationTest
{
    @Test
    public void getParametersTest()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtable", "All records", "CustomOperation", "0", "{}");

        oneAssert(generate);
    }

    @Test
    public void getParametersReload()
    {
        Either<Object, OperationResult> generate = generateOperation(
                "testtable", "All records", "CustomOperation", "0", "{'name':'','value':'2'}");

        oneAssert(generate);
    }

    void oneAssert(Either<Object, OperationResult> generate){
        assertEquals("{'values':{'name':'','value':'4'},'meta':{'/name':{'displayName':'name','columnSize':'20'},'/value':{'displayName':'value','readOnly':true,'columnSize':'30'}},'order':['/name','/value']}",
                oneQuotes(JsonFactory.bean(generate.getFirst())));
    }
}