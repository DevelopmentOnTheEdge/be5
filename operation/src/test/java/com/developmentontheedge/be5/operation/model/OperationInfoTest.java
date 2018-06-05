package com.developmentontheedge.be5.operation.model;

import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class OperationInfoTest extends OperationsSqlMockProjectTest
{
    @Test
    public void getModel() throws Exception
    {
        OperationInfo operationInfo = new OperationInfo(meta.getOperation("testtable", "CustomOperation"));

        assertEquals("CustomOperation", operationInfo.getModel().getName());
        assertEquals("CustomOperation", operationInfo.getName());
        assertEquals("Administrator", operationInfo.getRoles().getFinalRolesString());
        assertEquals("Groovy", operationInfo.getType());
        assertEquals(0, operationInfo.getRecords());
        assertEquals("", operationInfo.getLayout());

        assertEquals("Always", operationInfo.getVisibleWhen());
        assertEquals(999999, operationInfo.getExecutionPriority());
        assertEquals("none", operationInfo.getLogging());
        assertEquals(null, operationInfo.getCategoryID());
    }

}