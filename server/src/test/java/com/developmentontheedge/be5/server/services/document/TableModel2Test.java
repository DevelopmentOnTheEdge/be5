package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.test.BaseTestUtils;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class TableModel2Test extends ServerBe5ProjectDBTest
{
    @Inject
    private DocumentGenerator documentGenerator;

    private Long user1ID;
    private Long user2ID;

    @Before
    public void testTableQueryDBTestBefore()
    {
        setStaticUserInfo(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        db.update("delete from testtable");
        user1ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user1", 2L);
        user2ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user2", 1L);
    }

    @Test
    public void testData()
    {
        InputStream data = new ByteArrayInputStream("blob data".getBytes(StandardCharsets.UTF_8));
        db.insert("INSERT INTO testData (name, textCol, dataCol) VALUES (?, ?, ?)", "test name", "test text", data);

        Query query = meta.getQuery("testData", "All records");
        TablePresentation table = documentGenerator.getTablePresentation(query, Collections.emptyMap());
        Assert.assertEquals("{'cells':[" +
                        "{'content':'test name','options':{}}," +
                        "{'content':'test text','options':{}}," +
                        "{'content':'Blob','options':{}}]}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getRows().get(0))));
        db.update("DELETE FROM testData");
    }

    @Test
    public void beQuick()
    {
        Query query = meta.getQuery("testtable", "beQuick");
        TablePresentation table = documentGenerator.getTablePresentation(query, Collections.emptyMap());
        Assert.assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getRows().get(0))));
        Assert.assertEquals("[{'name':'Name','quick':'yes','title':'Name'}]",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getColumns())));
    }

    @Test
    public void beNoSort()
    {
        Query query = meta.getQuery("testtable", "beNoSort");
        TablePresentation table = documentGenerator.getTablePresentation(query, Collections.emptyMap());
        Assert.assertEquals("{'cells':[{'content':'user1','options':{}}],'id':'123'}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getRows().get(0))));
        Assert.assertEquals("[{'name':'Name','nosort':true,'title':'Name'}]",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getColumns())));
    }

    @Test
    public void withID()
    {
        Query query = meta.getQuery("testtable", "withID");
        TablePresentation table = documentGenerator.getTablePresentation(query, Collections.emptyMap());
        Assert.assertEquals("{'cells':[{'content':'user1','options':{}},{'content':'2','options':{}}]," +
                        "'id':'"+user1ID+"'}",
                BaseTestUtils.oneQuotes(BaseTestUtils.jsonb.toJson(table.getRows().get(0))));
    }

}
