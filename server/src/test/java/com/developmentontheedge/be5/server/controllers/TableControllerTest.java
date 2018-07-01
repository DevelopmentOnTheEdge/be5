package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;


public class TableControllerTest extends ServerBe5ProjectTest
{
    @Inject private TableController tableController;

    @Before
    public void setUp()
    {
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void generate()
    {
        JsonApiModel jsonApiModel = tableController.generate(getSpyMockRequest("/api/table/", ImmutableMap.of(
                RestApiConstants.ENTITY,"testtable",
                RestApiConstants.QUERY,"All records",
                RestApiConstants.TIMESTAMP_PARAM,"" + new Date().getTime())), "");

        assertEquals("testtable: All records", ((TablePresentation)jsonApiModel.getData().getAttributes()).getTitle());
    }

}