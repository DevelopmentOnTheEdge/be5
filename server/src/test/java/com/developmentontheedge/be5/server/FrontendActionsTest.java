package com.developmentontheedge.be5.server;

import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;

import static com.developmentontheedge.be5.server.FrontendActions.SET_URL;
import static org.junit.Assert.*;


public class FrontendActionsTest extends ServerBe5ProjectTest
{
    @Inject private DocumentGenerator documentGenerator;

    @Test
    public void setUrl()
    {
        assertEquals(new FrontendAction(SET_URL, "table/testtable/Test 1D"),
                FrontendActions.setUrl("table/testtable/Test 1D"));
    }

    @Test
    public void updateDocument()
    {
        FrontendAction frontendAction = FrontendActions.updateDocument(documentGenerator.
                createStaticPage("Title", "text", "url"));
        JsonApiModel jsonApiModel = (JsonApiModel) frontendAction.getValue();

        assertEquals("static", jsonApiModel.getData().getType());
    }
}
