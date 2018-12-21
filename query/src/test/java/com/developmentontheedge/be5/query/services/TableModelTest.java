package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.TableModel;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;

import static com.developmentontheedge.be5.base.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;
import static org.junit.Assert.assertEquals;


public class TableModelTest extends QueryBe5ProjectDBTest
{
    @Inject
    private DbService db;
    @Inject
    private ProjectProvider projectProvider;
    @Inject
    private TableModelService tableModelService;

    private Long user1ID;
    private Long user2ID;

    @Before
    public void testTableQueryDBTestBefore()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        db.update("delete from testtable");
        user1ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user1", 1L);
        user2ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user2", 2L);

        db.update("delete from testSubQuery");
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 2L);
    }

    @Test
    public void subQuery()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries().get("Sub Query");
        TableModel tableModel = tableModelService.getTableModel(query, new HashMap<>());

        assertEquals("{'content':'1<br/> 2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void dictionaryLocalization()
    {
        Query query = projectProvider.get().getEntity("testTags").getQueries().get("dictionaryLocalization");
        TableModel tableModel = tableModelService.getTableModel(query, new HashMap<>());

        assertEquals("'Региональный'", oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(0).content)));
        assertEquals("'Региональный'", oneQuotes(jsonb.toJson(tableModel.getRows().get(1).getCells().get(0).content)));
        assertEquals("'Муниципальный'", oneQuotes(jsonb.toJson(tableModel.getRows().get(2).getCells().get(0).content)));
        assertEquals("'Муниципальный'", oneQuotes(jsonb.toJson(tableModel.getRows().get(3).getCells().get(0).content)));
    }

    @Test
    public void subQueryDefault()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries().get("Sub Query default");
        TableModel tableModel = tableModelService.getTableModel(query, new HashMap<>());

        assertEquals("{'content':'defaultValue','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(0))));
    }

    @Test
    public void subQueryWithPrepareParams()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries()
                .get("Sub Query with prepare params");
        TableModel tableModel = tableModelService.getTableModel(query, new HashMap<>());

        assertEquals("{'content':'1<br/> 2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void subQueryWithLongPrepareParams()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries()
                .get("Sub Query with long prepare params");
        TableModel tableModel = tableModelService.getTableModel(query, new HashMap<>());
        assertEquals("{'content':'1','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void groovyTableTest()
    {
        TableModel tableModel = tableModelService.
                getTableModel(meta.getQuery("testtableAdmin", "TestGroovyTable"), new HashMap<>());

        assertEquals("[{'name':'name','title':'name'},{'name':'value','title':'value'}]", oneQuotes(jsonb.toJson(tableModel.getColumns())));

        assertEquals("[{'cells':[{'content':'a1','options':{}},{'content':'b1','options':{}}],'id':'1'}," +
                        "{'cells':[{'content':'a2','options':{}},{'content':'b2','options':{}}],'id':'2'}]"
                , oneQuotes(jsonb.toJson(tableModel.getRows())));
    }

    @Test
    public void emptyQuery()
    {
        Query query = meta.getQuery("testtable", "emptyQuery");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals(0, table.getRows().size());
    }

    @Test
    public void beLink()
    {
        Query query = meta.getQuery("testtable", "beLink");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'class':null,'url':'table/testtable/Test 1D unknown/ID=123/entity=users'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0).getCells().get(0).options.get("link"))));
    }

    @Test
    public void beLinkMultiple()
    {
        Query query = meta.getQuery("testtable", "beLinkMultiple");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'class':null,'url':'table/testtable/Test 1D unknown/ID=1,2'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0).getCells().get(0).options.get("link"))));
    }

    @Test
    public void beRef()
    {
        Query query = meta.getQuery("testtable", "beRef");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'class':null,'url':'table/testtable/All records'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0).getCells().get(0).options.get("link"))));
    }

    @Test
    public void beQuick()
    {
        Query query = meta.getQuery("testtable", "beQuick");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("[{'name':'Name','quick':'yes','title':'Name'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));
    }

    @Test
    public void beCssFormat()
    {
        Query query = meta.getQuery("testtable", "beCssFormat");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':1,'options':{'format':{'mask':'###,###,##0.00'},'css':{'class':'currency'}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void beRoles()
    {
        Query query = meta.getQuery("testtable", "beRoles");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("{'cells':[{'content':'user2','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(1))));

        setStaticUserInfo("TestUser2");
        TableModel table2 = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':1,'options':{'roles':{'name':'TestUser2'}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table2.getRows().get(0))));
    }

    @Test
    public void beRolesNot()
    {
        Query query = meta.getQuery("testtable", "beRolesNot");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':1,'options':{'roles':{'name':'!TestUser2'}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));

        setStaticUserInfo("TestUser2");
        TableModel table2 = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table2.getRows().get(0))));
    }

    @Test
    public void beGrouping()
    {
        Query query = meta.getQuery("testtable", "beGrouping");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{'grouping':{}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void beRowCssClass()
    {
        Query query = meta.getQuery("testtable", "beRowCssClass");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("[{'cells':[{'content':1,'options':{'css':{'class':' user1'}}}],'id':'123'}," +
                        "{'cells':[{'content':2,'options':{'css':{'class':' user2'}}}],'id':'123'}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void beAggregate()
    {
        Query query = meta.getQuery("testtable", "beAggregate");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':3.0,'options':{'format':{'mask':'###,###,##0.00'}," +
                        "'css':{'class':'currency'}}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(2))));
    }

    @Test
    public void beAggregate1D()
    {
        Query query = meta.getQuery("testtable", "beAggregate1D");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'Итого','options':{}},{'options':{}}," +
                        "{'content':3.0,'options':{" +
                            "'format':{'mask':'###,###,##0.00'},'css':{'class':'currency'}}}],'id':'aggregate'}",
                oneQuotes(jsonb.toJson(table.getRows().get(2))));
    }

    @Test
    public void subQueryTestNull()
    {
        db.update("DELETE FROM testtableAdmin");
        long id1 = db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 11);
        long id2 = db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", null);

        TableModel table = tableModelService.getTableModel(meta.getQuery("testtableAdmin", "Test null in subQuery"), Collections.emptyMap());

        assertEquals("[{'cells':[{'content':'tableModelTest','options':{}},{'content':11,'options':{}}," +
                        "{'content':'tableModelTest','options':{}}],'id':'"+id1+"'}," +
                        "{'cells':[{'content':'tableModelTest','options':{}}," +
                        "{'options':{}},{'content':'','options':{}}],'id':'"+id2+"'}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void withID()
    {
        Query query = meta.getQuery("testtable", "withID");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':1,'options':{}}]," +
                        "'id':'"+user1ID+"'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void withOrder()
    {
        Query query = meta.getQuery("testtable", "All records");
        TableModel table = tableModelService.getTableModel(query, ImmutableMap.
                of(ORDER_COLUMN, "1", ORDER_DIR, "desc"));
        assertEquals("{'cells':[{'content':'user2','options':{}},{'content':2,'options':{}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void withCategory()
    {
        db.insert("insert into classifications (categoryID, recordID) VALUES (?, ?)", 123, "testtable." + user2ID);
        Query query = meta.getQuery("testtable", "All records");
        TableModel table = tableModelService.getTableModel(query, ImmutableMap.
                of(CATEGORY_ID_PARAM, "123"));
        assertEquals("{'cells':[{'content':'user2','options':{}},{'content':2,'options':{}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        db.update("DELETE FROM classifications");
    }

    @Test
    public void multipleValue()
    {
        Query query = meta.getQuery("testtable", "All records");
        TableModel table = tableModelService.getTableModel(query, Collections.singletonMap("name", new String[]{"user1", "user2"}));

        assertEquals(2, table.getRows().size());
        assertEquals("user1", table.getRows().get(0).getCells().get(0).content);
        assertEquals("user2", table.getRows().get(1).getCells().get(0).content);
    }
}
