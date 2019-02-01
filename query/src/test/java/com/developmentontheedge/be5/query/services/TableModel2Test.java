package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.TableModel;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TableModel2Test extends QueryBe5ProjectDBTest
{
    @Inject
    private TableModelService tableModelService;

    private Long user1ID;
    private Long user2ID;

    @Before
    public void testTableQueryDBTestBefore()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        db.update("delete from testtable");
        user1ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user1", 2L);
        user2ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user2", 1L);

        db.update("delete from testSubQuery");
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 2L);
    }

    @Test
    public void testData()
    {
        InputStream data = new ByteArrayInputStream("blob data".getBytes(StandardCharsets.UTF_8));
        db.insert("INSERT INTO testData (name, textCol, dataCol) VALUES (?, ?, ?)", "test name", "test text", data);

        Query query = meta.getQuery("testData", "All records");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[" +
                        "{'content':'test name','options':{}}," +
                        "{'content':'test text','options':{}}," +
                        "{'content':'Blob','options':{}}]}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        db.update("DELETE FROM testData");
    }

    @Test
    public void beQuick()
    {
        Query query = meta.getQuery("testtable", "beQuick");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("[{'name':'Name','quick':'yes','title':'Name'}]",
                oneQuotes(jsonb.toJson(table.getColumns())));
    }

    @Test
    public void beRoles()
    {
        Query query = meta.getQuery("testtable", "beRoles");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
        assertEquals("{'cells':[{'content':'user2','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(1))));

        setStaticUserInfo("TestUser2");
        TableModel table2 = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':2,'options':{'roles':{'name':'TestUser2'}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table2.getRows().get(0))));
    }

    @Test
    public void beRolesNot()
    {
        Query query = meta.getQuery("testtable", "beRolesNot");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':2,'options':{'roles':{'name':'!TestUser2'}}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));

        setStaticUserInfo("TestUser2");
        TableModel table2 = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                oneQuotes(jsonb.toJson(table2.getRows().get(0))));
    }

    @Test
    public void beRowCssClass()
    {
        Query query = meta.getQuery("testtable", "beRowCssClass");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("[{'cells':[{'content':2,'options':{'css':{'class':' user1'}}}],'id':'123'}," +
                        "{'cells':[{'content':1,'options':{'css':{'class':' user2'}}}],'id':'123'}]",
                oneQuotes(jsonb.toJson(table.getRows())));
    }

    @Test
    public void withID()
    {
        Query query = meta.getQuery("testtable", "withID");
        TableModel table = tableModelService.create(query, Collections.emptyMap());
        assertEquals("{'cells':[{'content':'user1','options':{}},{'content':2,'options':{}}]," +
                        "'id':'"+user1ID+"'}",
                oneQuotes(jsonb.toJson(table.getRows().get(0))));
    }

}
