package com.developmentontheedge.be5.operation;

import com.developmentontheedge.be5.api.services.OperationsFactory;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.LinkedHashMap;


public class OperationsFactoryTest extends OperationsSqlMockProjectTest
{
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void init()
    {
    }

    @Test
    public void generateErrorInPropertyOnExecute()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Error in property (getParameters)");
        executeAndCheck("generateErrorInProperty");
    }

    @Test
    public void generateErrorStatusOnExecute()
    {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("The operation can not be performed.");
        executeAndCheck("generateErrorStatus");
    }

    @Test
    public void generateErrorOnExecute()
    {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Internal error occured during operation testEntity.ErrorProcessing");
        executeAndCheck("generateError");
    }

    @Test
    public void executeErrorInProperty()
    {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error in property (invoke)");
        executeAndCheck("executeErrorInProperty");
    }

    @Test
    public void executeErrorStatus()
    {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("An error occurred while performing operations.");
        executeAndCheck("executeErrorStatus");
    }

    @Test
    public void executeError()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Internal error occured during operation testEntity.ErrorProcessing");
        executeAndCheck("executeError");
    }

    @Test
    public void executeOperationWithoutParams()
    {
        expectedEx.expect(Be5Exception.class);
        expectedEx.expectMessage("Internal error occured during operation testEntity.ErrorProcessing");
        executeAndCheck("withoutParams");
    }

    public void executeAndCheck(String value)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
        map.put("name", value);
        Operation operation = operations.get("testEntity", "ErrorProcessing").setPresetValues(map).execute();

        Assert.assertEquals(OperationStatus.ERROR, operation.getStatus());
    }

    public OperationsFactory getOperations()
    {
        return operations;
    }

    public void setOperations(OperationsFactory operations)
    {
        this.operations = operations;
    }

}
