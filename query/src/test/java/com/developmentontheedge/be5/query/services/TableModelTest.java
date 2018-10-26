package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.TableModel;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class TableModelTest extends QueryBe5ProjectDBTest
{
    @Inject
    private DbService db;
    @Inject
    private ProjectProvider projectProvider;
    @Inject
    private TableModelService tableModelService;

    @Before
    public void testTableQueryDBTestBefore()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        db.update("delete from testtable");
        db.insert("insert into testtable (name, value) VALUES (?, ?)", "user1", 1L);

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
                        "'link':{'url':'table/testtable/Test 1D unknown/ID=123'}}}]}",
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
    public void testNullInSubQuery()
    {
        db.update("DELETE FROM testtableAdmin");
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 11);
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", null);

        TableModel table = tableModelService.getTableModel(meta.getQuery("testtableAdmin", "Test null in subQuery"), Collections.emptyMap());

        assertEquals("[" + "{'cells':[" + "{'content':'tableModelTest','options':{}}," + "{'content':11,'options':{}}," + "{'content':'tableModelTest','options':{}}]}," + "{'cells':[" + "{'content':'tableModelTest','options':{}}," + "{'options':{}}," + "{'content':'','options':{}}" + "]}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

}
