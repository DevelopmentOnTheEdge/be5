package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import static com.developmentontheedge.be5.server.FrontendActions.SET_URL;
import static org.junit.Assert.assertEquals;


public class FrontendActionsTest extends ServerBe5ProjectTest
{
    @Test
    public void setUrl()
    {
        assertEquals(new FrontendAction(SET_URL, "table/testtable/Test 1D"),
                FrontendActions.setUrl("table/testtable/Test 1D"));
    }
}
