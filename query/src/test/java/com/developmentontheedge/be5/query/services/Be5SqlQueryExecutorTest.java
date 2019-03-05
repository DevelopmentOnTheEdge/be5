package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.List;

import static com.developmentontheedge.be5.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;
import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class Be5SqlQueryExecutorTest extends QueryBe5ProjectDBTest
{
    @Inject private QueryExecutorFactory queryExecutorFactory;

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
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("test name", list.get(0).getValue("name"));
        assertEquals("test text", list.get(0).getValue("textCol"));
        assertTrue(list.get(0).getValue("dataCol") instanceof Blob);

        db.update("DELETE FROM testData");
    }

    @Test
    public void subQuery()
    {
        Query query = meta.getQuery("testtable", "Sub Query");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("1<br/> 2", list.get(0).getValue("testSubQueryValues"));
    }

    @Test
    public void subQueryDefault()
    {
        Query query = meta.getQuery("testtable", "Sub Query default");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("defaultValue", list.get(0).getValue("testSubQueryValues"));
    }

    @Test
    public void subQueryWithPrepareParams()
    {
        Query query = meta.getQuery("testtable", "Sub Query with prepare params");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("1<br/> 2", list.get(0).getValue("testtUserValues"));
    }

    @Test
    public void subQueryWithLongPrepareParams()
    {
        Query query = meta.getQuery("testtable", "Sub Query with long prepare params");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("2", list.get(0).getValue("testtUserValues"));
    }

    @Test
    public void groovyTableTest()
    {
        Query query = meta.getQuery("testtableAdmin", "TestGroovyTable");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();

        assertEquals("a1", list.get(0).getValue("name"));
        assertEquals("b1", list.get(0).getValue("value"));
        assertEquals("a2", list.get(1).getValue("name"));
        assertEquals("b2", list.get(1).getValue("value"));
        assertEquals("name", list.get(0).getProperty("name").getDisplayName());
    }

    @Test
    public void emptyQuery()
    {
        Query query = meta.getQuery("testtable", "emptyQuery");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals(0, list.size());
    }

    @Test
    public void beLink()
    {
        Query query = meta.getQuery("testtable", "beLink");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("{class=null, url=table/testtable/Test 1D unknown/ID=123/entity=users}",
                DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("link").toString());
    }

    @Test
    public void beLinkMultiple()
    {
        Query query = meta.getQuery("testtable", "beLinkMultiple");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("{class=null, url=table/testtable/Test 1D unknown/ID=1,2}",
                DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("link").toString());
    }

    @Test
    public void beRef()
    {
        Query query = meta.getQuery("testtable", "beRef");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("{class=null, url=table/testtable/All records}",
                DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("link").toString());
    }

    @Test
    public void beQuick()
    {
        Query query = meta.getQuery("testtable", "beQuick");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("true", DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("quick").get("visible"));
    }

    @Test
    public void beCssFormat()
    {
        Query query = meta.getQuery("testtable", "beCssFormat");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();

        assertEquals("###,###,##0.00",
                DynamicPropertyMeta.get(list.get(0).getProperty("Value")).get("format").get("mask"));
        assertEquals("currency",
                DynamicPropertyMeta.get(list.get(0).getProperty("Value")).get("css").get("class"));
    }

    @Test
    public void beAggregate()
    {
        Query query = meta.getQuery("testtable", "beAggregate");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();

        assertEquals(3.0, list.get(2).getValue("Value"));
        assertEquals("###,###,##0.00",
                DynamicPropertyMeta.get(list.get(2).getProperty("Value")).get("format").get("mask"));
        assertEquals("currency",
                DynamicPropertyMeta.get(list.get(2).getProperty("Value")).get("css").get("class"));
    }

    @Test
    public void beRowCssClass()
    {
        Query query = meta.getQuery("testtable", "beRowCssClass");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();

        assertEquals(" user1", DynamicPropertyMeta.get(list.get(0).getProperty("test")).get("css").get("class"));
        assertEquals(" user1", DynamicPropertyMeta.get(list.get(0).getProperty("value")).get("css").get("class"));

        assertEquals(" user2", DynamicPropertyMeta.get(list.get(1).getProperty("value")).get("css").get("class"));
    }

    @Test
    public void beGrouping()
    {
        Query query = meta.getQuery("testtable", "beGrouping");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("{}",
                DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("grouping").toString());
    }

    @Test
    public void beNoSort()
    {
        Query query = meta.getQuery("testtable", "beNoSort");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals("{}",
                DynamicPropertyMeta.get(list.get(0).getProperty("Name")).get("nosort").toString());
    }

    @Test
    public void beAggregate1D()
    {
        Query query = meta.getQuery("testtable", "beAggregate1D");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertEquals(3.0, list.get(2).getValue("Value"));
        assertEquals("###,###,##0.00",
                DynamicPropertyMeta.get(list.get(2).getProperty("Value")).get("format").get("mask"));
        assertEquals("currency",
                DynamicPropertyMeta.get(list.get(2).getProperty("Value")).get("css").get("class"));
    }

    @Test
    public void subQueryTestNull()
    {
        db.update("DELETE FROM testtableAdmin");
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", 11);
        db.insert("insert into testtableAdmin (name, value) VALUES (?, ?)", "tableModelTest", null);

        Query query = meta.getQuery("testtableAdmin", "Test null in subQuery");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();

        assertEquals("tableModelTest", list.get(0).getValue("NameFromSubQuery"));
        assertEquals("", list.get(1).getValue("NameFromSubQuery"));
    }

    @Test
    public void order()
    {
        Query query = meta.getQuery("testtable", "All records");
        List<QRec> list = queryExecutorFactory.get(query, of(ORDER_COLUMN, "2", ORDER_DIR, "desc")).execute();
        assertEquals("user1", list.get(0).getValue("Name"));
        assertEquals(2L, list.get(0).getValue("Value"));
    }

    @Test
    public void testOrderWithHiddenColumns()
    {
        Query query = meta.getQuery("testtable", "testOrderWithHiddenColumns");
        List<QRec> list = queryExecutorFactory.get(query, of(ORDER_COLUMN, "2", ORDER_DIR, "desc")).execute();
        assertEquals("user1", list.get(0).getValue("Name"));
        assertEquals(2L, list.get(0).getValue("Value"));
    }

    @Test
    public void withCategory()
    {
        db.insert("insert into classifications (categoryID, recordID) VALUES (?, ?)", 123, "testtable." + user2ID);
        Query query = meta.getQuery("testtable", "All records");
        List<QRec> list = queryExecutorFactory.get(query, of(CATEGORY_ID_PARAM, "123")).execute();
        assertEquals("user2", list.get(0).getValue("Name"));
        assertEquals(1L, list.get(0).getValue("Value"));
        db.update("DELETE FROM classifications");
    }

    @Test
    public void multipleValue()
    {
        Query query = meta.getQuery("testtable", "All records");
        List<QRec> list = queryExecutorFactory.get(query, singletonMap("name", new String[]{"user1", "user2"})).execute();

        assertEquals(2, list.size());
        assertEquals("user1", list.get(0).getValue("Name"));
        assertEquals("user2", list.get(1).getValue("Name"));
    }

    @Test
    public void sqlSubQuery()
    {
        Query query = meta.getQuery("testtable", "sqlSubQuery");
        List<QRec> list = queryExecutorFactory.get(query, singletonMap("name", "value")).execute();
        assertEquals("user2", list.get(1).getValue("Name"));
        assertEquals(1L, list.get(1).getValue("Value"));
    }

    @Test
    public void beRoles()
    {
        Query query = meta.getQuery("testtable", "beRoles");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertNull(list.get(0).getProperty("Value"));

        setStaticUserInfo("TestUser2");
        List<QRec> list2 = queryExecutorFactory.get(query, emptyMap()).execute();
        assertNotNull(list2.get(0).getProperty("Value"));
    }

    @Test
    public void beRolesNot()
    {
        Query query = meta.getQuery("testtable", "beRolesNot");
        List<QRec> list = queryExecutorFactory.get(query, emptyMap()).execute();
        assertNotNull(list.get(0).getProperty("Value"));

        setStaticUserInfo("TestUser2");
        List<QRec> list2 = queryExecutorFactory.get(query, emptyMap()).execute();
        assertNull(list2.get(0).getProperty("Value"));
    }
}
