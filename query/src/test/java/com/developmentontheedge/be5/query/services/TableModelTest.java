package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.TableModel;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;


public class TableModelTest extends QueryBe5ProjectDBTest
{
    @Inject private DbService db;
    @Inject private ProjectProvider projectProvider;
    @Inject private TableModelService tableModelService;

    @Before
    public void testTableQueryDBTestBefore()
    {
        db.update("delete from testtable");
        db.insert("insert into testtable (name, value) VALUES (?, ?)","tableModelTest", "1");

        db.update("delete from testSubQuery");
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)","tableModelTest", "user1");
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)","tableModelTest", "user2");
    }

    @Test
    public void testExecuteSubQuery()
    {
        Query query = projectProvider.getProject().getEntity("testtable").getQueries().get("Sub Query");
        TableModel tableModel = tableModelService.builder(query, new HashMap<>())
                .limit(20)
                .build();

        assertEquals("{'content':'user1<br/> user2','options':{}}",
                oneQuotes(jsonb.toJson(tableModel.getRows().get(0).getCells().get(2))));
    }

    @Test
    public void groovyTableTest() throws Exception
    {
        TableModel tableModel = tableModelService.
                getTableModel(meta.getQuery("testtableAdmin", "TestGroovyTable"), new HashMap<>());

        assertEquals("[{'name':'name','title':'name'},{'name':'value','title':'value'}]", oneQuotes(jsonb.toJson(tableModel.getColumns())));

        assertEquals("[{'cells':[{'content':'a1','options':{}},{'content':'b1','options':{}}],'id':'0'}," +
                        "{'cells':[{'content':'a2','options':{}},{'content':'b2','options':{}}],'id':'0'}]"
                , oneQuotes(jsonb.toJson(tableModel.getRows())));
    }

}
