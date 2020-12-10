package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.services.TestTableQueryDBTest;
import com.developmentontheedge.be5.server.services.events.Be5EventTestLogger;
import com.developmentontheedge.be5.test.ServerTestResponse;
import com.developmentontheedge.be5.test.mocks.CoreUtilsForTest;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DocumentGeneratorTest extends TestTableQueryDBTest
{
    @Inject private Meta meta;
    @Inject private DocumentGenerator documentGenerator;

    @Before
    public void setUp()
    {
        Be5EventTestLogger.clearMock();
        initGuest();
        ServerTestResponse.newMock();
    }

    @Test
    public void getTablePresentation()
    {
        Query query = meta.getQuery("testtable", "All records");
        TablePresentation table = documentGenerator.getTablePresentation(query, emptyMap());

        assertEquals("[{'name':'Name','title':'Name'},{'name':'Value','title':'Value'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));

        assertEquals("[{'cells':[" +
                        "{'content':'tableModelTest','options':{}}," +
                        "{'content':'1','options':{}}" + "]}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void getTableRowsAsJson()
    {
        JsonApiModel table = documentGenerator.getTableRowsAsJson("testtable", "All records", emptyMap());

        assertEquals("{'attributes':{'data':[{'Name':'tableModelTest','Value':'1'}]},'type':'json'}",
                oneQuotes(jsonb.toJson(table.getData())));
    }

    @Test
    public void getTableTotalNumberOfRows()
    {
        JsonApiModel table = documentGenerator.getTableTotalNumberOfRows("testtable", "All records", emptyMap());

        assertEquals("{'attributes':{'totalNumberOfRows':1},'type':'number_of_rows'}",
                oneQuotes(jsonb.toJson(table.getData())));
    }

    @Test
    public void testTitleAllRecords()
    {
        JsonApiModel jsonApiModel = documentGenerator.getDocument(meta.getQuery("testtable", "All records"), emptyMap());
        TablePresentation table = (TablePresentation) jsonApiModel.getData().getAttributes();
        assertEquals("Testtable", table.getTitle());
    }

    @Test
    public void getLimit()
    {
        initUserWithNameAndRoles(RoleType.ROLE_TEST_USER,RoleType.ROLE_TEST_USER);
        DocumentGeneratorImpl documentGeneratorImpl = (DocumentGeneratorImpl) documentGenerator;
        Query query = meta.getQuery("testtable", "All records");

        assertEquals(10, documentGeneratorImpl.getLimit(query, emptyMap(), emptyMap()));

        assertEquals(20, documentGeneratorImpl.getLimit(query, emptyMap(), singletonMap(LIMIT, "20")));
        verify(CoreUtilsForTest.mock).setQuerySettingForUser("testtable", "All records",
                RoleType.ROLE_TEST_USER, Collections.singletonMap("recordsPerPage", 20));

        when(CoreUtilsForTest.mock.getQuerySettingForUser("testtable", "All records",
                RoleType.ROLE_TEST_USER)).thenReturn(Collections.singletonMap("recordsPerPage", 20));
        assertEquals(20, documentGeneratorImpl.getLimit(query, emptyMap(), emptyMap()));

        CoreUtilsForTest.clearMock();
        assertEquals(10, documentGeneratorImpl.getLimit(query, emptyMap(), emptyMap()));
    }

    @Test
    public void getLimitForGuestUser()
    {
        DocumentGeneratorImpl documentGeneratorImpl = (DocumentGeneratorImpl) documentGenerator;
        Query query = meta.getQuery("testtable", "All records");

        assertEquals(10, documentGeneratorImpl.getLimit(query, emptyMap(), emptyMap()));
        assertEquals(20, documentGeneratorImpl.getLimit(query, emptyMap(), singletonMap(LIMIT, "20")));
        verify(CoreUtilsForTest.mock, never()).setQuerySettingForUser("testtable", "All records",
                RoleType.ROLE_GUEST, Collections.emptyMap());
    }

    @Test
    public void testTitle()
    {
        initUserWithRoles("SystemDeveloper");
        JsonApiModel jsonApiModel = documentGenerator.getDocument(
                meta.getQuery("testtable", "TableWithFilter"), emptyMap());
        TablePresentation table = (TablePresentation) jsonApiModel.getData().getAttributes();
        assertEquals("Testtable: TableWithFilter", table.getTitle());
    }

    @Test
    public void testTableWithFilterOperation()
    {
        initUserWithRoles("SystemDeveloper");

        Query query = meta.getQuery("testtable", "TableWithFilter");

        JsonApiModel document = documentGenerator.getDocument(query, emptyMap());

        assertEquals("{'attributes':{'category':'testtable','columns':[{'name':'1','title':'1'}],'layout':{'topForm':'FilterByParamsInQueryOperation'}," +
                        "'length':10,'offset':0,'orderColumn':-1,'orderDir':'asc'," +
                        "'page':'TableWithFilter','parameters':{},'rows':[{'cells':[{'content':1,'options':{}}]}],'selectable':false,'title':'Testtable: TableWithFilter','totalNumberOfRows':1},'links':{'self':'table/testtable/TableWithFilter'},'type':'table'}",
                oneQuotes(jsonb.toJson(document.getData())));

        assertEquals("{'attributes':{'bean':{'values':{'_search_presets_':'','_search_':true},'meta':{'/_search_presets_':{'displayName':'_search_presets_','hidden':true,'readOnly':true,'canBeNull':true},'/_search_':{'displayName':'_search_','type':'Boolean','hidden':true,'readOnly':true,'canBeNull':true}},'order':['/_search_presets_','/_search_']}," +
                        "'entity':'testtable','layout':{'type':'modalForm'},'operation':'FilterByParamsInQueryOperation','operationParams':{},'operationResult':{'status':'GENERATE','timeout':5},'query':'TableWithFilter','title':'FilterByParamsInQueryOperation'}," +
                        "'id':'topForm','links':{'self':'form/testtable/TableWithFilter/FilterByParamsInQueryOperation'},'type':'form'}",
                oneQuotes(jsonb.toJson(Arrays.stream(document.getIncluded()).filter(res -> "topForm".equals(res.getId())).findAny())));
    }

    @Test
    public void getQueryJsonApiForUser()
    {
        JsonApiModel queryJsonApiForUser = documentGenerator.getDocument("testtable", "All records", emptyMap());

        assertNotNull(queryJsonApiForUser.getData());
        TestCase.assertNull(queryJsonApiForUser.getErrors());
        verify(Be5EventTestLogger.mock).queryCompleted(any(), any(), anyLong(), anyLong());
    }

    @Test
    public void testSelfLink()
    {
        JsonApiModel jsonApiModel = documentGenerator.getDocument(meta.getQuery("testtable", "All records"),
                Collections.singletonMap("name", "1"));

        assertEquals("table/testtable/All records/name=1",
                jsonApiModel.getData().getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void testSelfLinkWithFilter()
    {
        JsonApiModel jsonApiModel = documentGenerator.getDocument(meta.getQuery("testtable", "All records"),
                of("name", "1", FrontendConstants.SEARCH_PARAM, "true"));

        assertEquals("table/testtable/All records",
                jsonApiModel.getData().getLinks().get(RestApiConstants.SELF_LINK));
    }

    @Test
    public void updateQueryJsonApi()
    {
        JsonApiModel jsonApiModel = documentGenerator.getNewTableRows("testtable", "All records", emptyMap());

        assertEquals("{'attributes':{'data':[[" +
                        "{'content':'','options':{}}," +
                        "{'content':'tableModelTest','options':{}}," +
                        "{'content':'1','options':{}}" +
                    "]]," +
                    "'recordsFiltered':1,'recordsTotal':1}," +
                    "'links':{'self':'table/testtable/All records'}," +
                    "'type':'table_more'}",
                oneQuotes(jsonb.toJson(jsonApiModel.getData())));
    }

    @Test
    public void logQueryErrorEvent()
    {
        try
        {
            documentGenerator.getDocument("testtable", "Query with error", emptyMap());
        }
        catch (Be5Exception ignore)
        {
        }
        verify(Be5EventTestLogger.mock).queryError(any(), any(), anyLong(), anyLong(), any());
    }
}
