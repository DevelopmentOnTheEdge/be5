package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.test.ServerTestResponse;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta;
    @Inject private DocumentGenerator documentGenerator;
    @Inject private OperationExecutor operationExecutor;

    @Before
    public void setUp()
    {
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void getTablePresentation()
    {
        TablePresentation table = documentGenerator.getTablePresentation(meta.getQuery("testtable", "All records"), Collections.emptyMap());

        assertEquals("testtable: All records", table.getTitle());

        assertEquals("[{'name':'Name','title':'Name'},{'name':'Value','title':'Value'}]", oneQuotes(jsonb.toJson(table.getColumns())));

        assertEquals("[{'cells':[" + "{'content':'tableModelTest','options':{}}," + "{'content':'1','options':{}}" + "]}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void testTableWithFilter()
    {
        initUserWithRoles("SystemDeveloper");

        Query query = meta.getQuery("testtable", "TableWithFilter");

        JsonApiModel document = documentGenerator.getJsonApiModel(query, Collections.emptyMap());

        assertEquals("{'attributes':{'category':'testtable','columns':[{'name':'1','title':'1'}],'hasAggregate':false,'layout':{'topForm':'FilterByParamsInQueryOperation'}," +
                        "'length':10,'offset':0,'orderColumn':-1,'orderDir':'asc'," +
                        "'page':'TableWithFilter','parameters':{},'rows':[{'cells':[{'content':1,'options':{}}]}],'selectable':false,'title':'testtable: TableWithFilter','totalNumberOfRows':1},'links':{'self':'table/testtable/TableWithFilter'},'type':'table'}",
                oneQuotes(jsonb.toJson(document.getData())));

        assertEquals("{'attributes':{'bean':{'values':{'_search_presets_':'','_search_':true},'meta':{'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/_search_presets_','/_search_']}," +
                        "'entity':'testtable','layout':{},'operation':'FilterByParamsInQueryOperation','operationParams':{},'operationResult':{'status':'generate'},'query':'TableWithFilter','title':'testtable: FilterByParamsInQueryOperation'}," +
                        "'id':'topForm','links':{'self':'form/testtable/TableWithFilter/FilterByParamsInQueryOperation'},'type':'form'}",
                oneQuotes(jsonb.toJson(Arrays.stream(document.getIncluded()).filter(res -> "topForm".equals(res.getId())).findAny())));
    }

    @Test
    public void getQueryJsonApiForUser()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.queryJsonApiFor("testtable", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        TestCase.assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    public void accessDenied()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.queryJsonApiFor("testtableAdmin", "All records", Collections.emptyMap());

        assertEquals(new ErrorModel("403", "Access denied to query: testtableAdmin.All records", Collections.singletonMap("self", "table/testtableAdmin/All records")),
                queryJsonApiForUser.getErrors()[0]);
    }

    @Test
    public void accessAllowed()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);

        JsonApiModel queryJsonApiForUser = documentGenerator.queryJsonApiFor("testtableAdmin", "All records", Collections.emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        TestCase.assertNull(queryJsonApiForUser.getErrors());
    }

    @Test
    public void error()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.queryJsonApiFor("testtable", "Query with error", Collections.emptyMap());

        assertNull(queryJsonApiForUser.getData());
        assertEquals(new ErrorModel("500", "Internal error occurred during query: testtable.Query with error", Collections.singletonMap("self", "table/testtable/Query with error")), queryJsonApiForUser.getErrors()[0]);
    }

    @Test
    public void testSelfLink()
    {
        JsonApiModel jsonApiModel = documentGenerator.getJsonApiModel(meta.getQuery("testtable", "All records"),
                Collections.singletonMap("name", "1"));

        assertEquals("table/testtable/All records/name=1",
                jsonApiModel.getData().getLinks().get(RestApiConstants.SELF_LINK));
    }
}
