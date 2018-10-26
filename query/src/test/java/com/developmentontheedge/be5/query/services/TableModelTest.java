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
import static com.developmentontheedge.be5.query.TableConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_DIR;
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
    public void testExecuteSubQuery()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries().get("Sub Query");
        TableModel tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build();

        assertEquals("{'content':'1<br/> 2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void subQueryDefault()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries().get("Sub Query default");
        TableModel tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build();

        assertEquals("{'content':'defaultValue','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(0))));
    }

    @Test
    public void testExecuteSubQueryWithPrepareParams()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries()
                .get("Sub Query with prepare params");
        TableModel tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build();

        assertEquals("{'content':'1<br/> 2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void testExecuteSubQueryWithLongPrepareParams()
    {
        Query query = projectProvider.get().getEntity("testtable").getQueries()
                .get("Sub Query with long prepare params");
        TableModel tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build();

        assertEquals("{'content':'1','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void groovyTableTest()
    {
        TableModel tableModel = tableModelService.
                getTableModel(meta.getQuery("testtableAdmin", "TestGroovyTable"), new HashMap<>());

        assertEquals("[{'name':'name','title':'name'},{'name':'value','title':'value'}]", oneQuotes(jsonb.toJson(tableModel.getColumns())));

        assertEquals("[{'cells':[{'content':'a1','options':{}},{'content':'b1','options':{}}],'id':'0'}," +
                        "{'cells':[{'content':'a2','options':{}},{'content':'b2','options':{}}],'id':'0'}]"
                , oneQuotes(jsonb.toJson(tableModel.getRows())));
    }

    @Test
    public void beLink()
    {
        Query query = meta.getQuery("testtable", "beLink");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{" +
                        "'link':{'url':'table/testtable/Test 1D unknown/entity=users/ID=123'}}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("[{'name':'Name','title':'Name'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));
    }

    @Test
    public void beQuick()
    {
        Query query = meta.getQuery("testtable", "beQuick");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("[{'name':'Name','quick':'yes','title':'Name'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));
    }

    @Test
    public void beCssFormat()
    {
        Query query = meta.getQuery("testtable", "beCssFormat");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':1,'options':{'format':{'mask':'###,###,##0.00'},'css':{'class':'currency'}}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void beRoles()
    {
        Query query = meta.getQuery("testtable", "beRoles");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("[{'name':'Name','title':'Name'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));

        setStaticUserInfo("TestUser2");
        TableModel table2 = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':1,'options':{'roles':{'name':'TestUser2'}}}]}",
                oneQuotes(jsonb.toJson(table2.getRows().get(0))));
        assertEquals("[{'name':'Name','title':'Name'},{'name':'Value','title':'Value'}]",
                oneQuotes(jsonb.toJson(table2.getColumns())));
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void beGrouping()
    {
        Query query = meta.getQuery("testtable", "beGrouping");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{'grouping':{}}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

    @Test
    public void beRowCssClass()
    {
        Query query = meta.getQuery("testtable", "beRowCssClass");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("[{'cells':[{'content':1,'options':{'css':{'class':' user1'}}}]}," +
                              "{'cells':[{'content':2,'options':{'css':{'class':' user2'}}}]}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void beAggregate()
    {
        Query query = meta.getQuery("testtable", "beAggregate");
        TableModel table = tableModelService.getTableModel(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':3.0,'options':{'css':{'class':'currency'}," +
                        "'format':{'mask':'###,###,##0.00'}}}],'id':'aggregate'}",
                oneQuotes(jsonb.toJson(table.getRows().get(2))));
    }

    @Test
    public void testNullInSubQuery()
    {
        db.update("DELETE FROM testtableAdmin");
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 11);
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", null);

        TableModel table = tableModelService.getTableModel(meta.getQuery("testtableAdmin", "Test null in subQuery"), Collections.emptyMap());

        assertEquals("[" + "{'cells':[" + "{'content':'tableModelTest','options':{}}," + "{'content':11,'options':{}}," + "{'content':'tableModelTest','options':{}}]}," + "{'cells':[" + "{'content':'tableModelTest','options':{}}," + "{'options':{}}," + "{'content':'','options':{}}" + "]}]",
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
}
