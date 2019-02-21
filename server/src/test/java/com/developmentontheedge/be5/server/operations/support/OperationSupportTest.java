package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.operation.OperationConstants;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.test.SqlMockOperationTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class OperationSupportTest extends SqlMockOperationTest
{
    private OperationSupport operationSupport;

    @Before
    public void setUp()
    {
        operationSupport = new OperationSupport() {
            @Override public void invoke(Object parameters) throws Exception { }
        };
        operationSupport.initialize(
                new OperationInfo(meta.getOperation("testtable", "CustomOperation")),
                new OperationContext(new Object[]{"1"}, "Test", Collections.singletonMap(OperationConstants.SELECTED_ROWS, "1")),
                OperationResult.create());
        getInjector().injectMembers(operationSupport);
    }

    @Test
    public void redirectThisOperation()
    {
        operationSupport.redirectThisOperation();
        FrontendAction[] actions = (FrontendAction[]) operationSupport.getResult().getDetails();
        assertEquals(FrontendActions.successAlert("Successfully completed."), actions[0]);
        assertEquals(FrontendActions.redirect("form/testtable/Test/CustomOperation/_selectedRows_=1"), actions[1]);
    }

    @Test
    public void setResultGoBack() throws Exception
    {
        operationSupport.setResultGoBack();
        FrontendAction[] actions = (FrontendAction[]) operationSupport.getResult().getDetails();
        assertEquals(FrontendActions.goBackOrRedirect("table/testtable/Test/_selectedRows_=1"), actions[0]);
    }

}
