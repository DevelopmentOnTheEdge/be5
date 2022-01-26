package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.config.ConfigurationProvider;
import com.developmentontheedge.be5.meta.ProjectProvider;

import com.developmentontheedge.be5.cache.Be5Caches;

import com.developmentontheedge.be5.database.DatabaseTest;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.adapters.ConcatColumnsParser;

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.github.benmanes.caffeine.cache.Cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class DbServiceTest extends DatabaseTest
{
    private static final ResultSetParser<TestPerson> parser = rs ->
            new TestPerson(rs.getLong("id"), rs.getString("name"),
                    rs.getString("password"), rs.getString("email"));

    @Inject private Be5Caches be5Caches;

    @BeforeClass
    public static void setUpClass()
    {
        DbService db = injector.getInstance(DbService.class);
        db.update("DELETE FROM persons");

        int update = db.update("INSERT INTO persons (name, password) VALUES (?,?)",
                "user1", "pass1");
        assertEquals(1, update);
        db.insert("INSERT INTO persons (name, password, email) VALUES (?,?,?)",
                "user2", "pass2", "email2@mail.ru");
    }

    @Test
    public void updateRaw()
    {
        int count = db.updateRaw("ALTER TABLE persons ADD column_test varchar(40)");

        assertEquals(0, count);
        assertNull(db.one("select column_test from persons"));
    }

    @Test
    public void executeRaw()
    {
        List<String> emails = db.executeRaw("SELECT email FROM persons WHERE name = ?",
                new ScalarHandler<>(), "user2");

        assertEquals(1, emails.size());
        assertEquals("email2@mail.ru", emails.get(0));
    }

    @Test
    public void testSelectScalar()
    {
        String password = db.one("SELECT password FROM persons WHERE name = ?", "user2");

        assertEquals("pass2", password);
    }

    @Test
    public void testCount()
    {
        assertEquals((Long) 0L, db.oneLong("SELECT COUNT(1) FROM persons WHERE name = ?", "notContainUser"));
    }

    @Test
    public void testCountMethod()
    {
        assertTrue(db.countFrom("SELECT COUNT(1) FROM persons") > 0);
        assertTrue(db.countFrom("persons WHERE 1 = 2") == 0);
        assertTrue(db.countFrom("persons") > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountMethodErrorSql()
    {
        assertTrue(db.countFrom("SELECT id FROM persons WHERE 1 = 2") == 0);
    }

    @Test
    public void oneInteger()
    {
        assertNotNull(db.oneInteger("SELECT CAST(id AS INT) FROM persons"));
    }

    @Test
    public void testSelectString()
    {
        assertEquals("pass2", db.oneString("SELECT password FROM persons WHERE name = ?",
                "user2"));
    }

    @Test
    public void testGetNullIfNotContain()
    {
        assertEquals(null, db.oneLong("SELECT id FROM persons WHERE name = ?", "notContainUser"));
    }

    @Test
    public void testBeanHandlerSelect()
    {
        TestPerson testPerson = db.query("SELECT * FROM persons WHERE name = ?", TestPerson.beanHandler
                , "user2");

        assertNotNull(testPerson);
        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void testResultSetParser()
    {
        TestPerson testPerson = db.select("SELECT * FROM persons WHERE name = ?", parser
                , "user2");

        assertNotNull(testPerson);
        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void testBeanHandlerGetNullIfNotContain()
    {
        TestPerson id = db.query("SELECT id FROM persons WHERE name = ?",
                TestPerson.beanHandler, "notContainUser");
        assertEquals(null, id);
    }

    @Test(expected = RuntimeException.class)
    public void testBeanHandlerError()
    {
        TestPerson id = db.query("SELECT id FROM2 persons WHERE name = ?",
                TestPerson.beanHandler, "notContainUser");
        assertEquals(null, id);
    }

    @Test
    public void testGetNullIfNotContainCustomObject()
    {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs ->
                rs.getString("email"), "notContainUser");
        assertEquals(null, email);
    }

    @Test
    public void testGetNullCustomObject()
    {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs ->
                rs.getString("email"), "user1");
        assertEquals(null, email);
    }

    @Test(expected = RuntimeException.class)
    public void testGetNullCustomObjectErrorCallNext()
    {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs -> {
            rs.next();
            return rs.getString("email");
        }, "user1");
        assertEquals(null, email);
    }

    @Test
    public void testSelectListBeanHandler()
    {
        List<TestPerson> persons = db.query("SELECT * FROM persons", TestPerson.beanListHandler);

        assertNotNull(persons);
        assertTrue(persons.size() >= 2);
        assertEquals("pass1", persons.get(0).getPassword());
        assertEquals("email2@mail.ru", persons.get(1).getEmail());
    }

    @Test
    public void testSelectListResultSetParser()
    {
        List<TestPerson> persons = db.list("SELECT * FROM persons", parser);

        assertTrue(persons.size() >= 2);
        assertEquals("pass1", persons.get(0).getPassword());
        assertEquals("email2@mail.ru", persons.get(1).getEmail());
    }

    @Test
    public void scalarLongList()
    {
        List<Long> persons = db.scalarLongList("SELECT id FROM persons");

        assertTrue(persons.size() >= 2);
        assertEquals(Long.class, persons.get(0).getClass());
    }

    @Test
    public void testSelectArrayLong()
    {
        Long[] persons = db.longArray("SELECT id FROM persons");

        assertTrue(persons.length >= 2);
        assertEquals(Long.class, persons[0].getClass());
    }

    @Test
    public void testSelectArrayString()
    {
        String[] persons = db.stringArray("SELECT name FROM persons");

        assertTrue(persons.length >= 2);
        assertEquals(String.class, persons[0].getClass());
    }

    @Test
    public void selectScalarList_Long()
    {
        List<Long> persons = db.scalarList("SELECT id FROM persons");

        assertTrue(persons.size() >= 2);
        assertEquals(Long.class, persons.get(0).getClass());
    }

    @Test
    public void selectScalarList_String()
    {
        List<String> persons = db.scalarList("SELECT name FROM persons");

        assertTrue(persons.size() >= 2);
        assertEquals(String.class, persons.get(0).getClass());
    }

    @Test
    public void testSelectListLambda()
    {
        List<String> strings = db.list("SELECT * FROM persons", rs ->
                rs.getString("name") + " "
                        + rs.getString("password")
        );

        assertTrue(strings.size() >= 2);
        assertEquals("user1 pass1", strings.get(0));
        assertEquals("user2 pass2", strings.get(1));
    }

    @Test
    public void testGetInsertId()
    {
        String uniqueName = "testGetInsertId";
        Long id = db.insert("INSERT INTO persons (name, password) VALUES (?,?)",
                uniqueName, "pass");

        assertEquals(id, db.oneLong("SELECT ID FROM persons WHERE name = ?", uniqueName));
    }

    @Test
    public void execute() throws SQLException
    {
        Map<String, Integer> metaData = db.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM persons"))
            {
                ResultSetMetaData metaData1 = ps.getMetaData();
                Map<String, Integer> map = new HashMap<>();
                map.put(metaData1.getColumnName(1), metaData1.getColumnType(1));
                map.put(metaData1.getColumnName(2), metaData1.getColumnType(2));
                return map;
            }
        });

        assertEquals(-5, (int) metaData.get("ID"));
        assertEquals(12, (int) metaData.get("NAME"));
    }

    @Test
    public void testConcatStringResultParser()
    {
        String row = db.select("SELECT name, password, email FROM persons WHERE name = ?", new ConcatColumnsParser(),"user2");
        assertEquals("user2,pass2,email2@mail.ru", row);
    }

    @Test
    public void testRecord()
    {
        QRec person = db.record( "SELECT * FROM persons WHERE name = 'user2'" );
        assertNotNull( person );        
        assertEquals("user2", person.getString( "name" ) );
        assertEquals("pass2", person.getString( "password" ) );
        assertEquals("email2@mail.ru", person.getString( "email" ) );
    }

    @Test
    public void testRecordCached()
    {
        Cache<Object, Object> test = be5Caches.getCache( "TestCache" );

        long hitCount = test.stats().hitCount();

        QRec person = db.record( "SELECT * FROM persons WHERE name = 'user2'", "TestCache" );
        assertNotNull( person );        
        assertEquals("user2", person.getString( "name" ) );
        assertEquals("pass2", person.getString( "password" ) );
        assertEquals("email2@mail.ru", person.getString( "email" ) );

        assertEquals( hitCount, test.stats().hitCount() );

        db.record( "SELECT * FROM persons WHERE name = 'user2'", "TestCache" );

        assertEquals( hitCount + 1, test.stats().hitCount() );
    }

    @Test
    public void testList()
    {
        List<QRec> persons = db.list( "SELECT * FROM persons ORDER BY name" );

        assertEquals( 2, persons.size() );

        assertEquals("user1", persons.get( 0 ).getString( "name" ) );
        assertEquals("pass1", persons.get( 0 ).getString( "password" ) );
        assertNull( persons.get( 0 ).getString( "email" ) );

        assertEquals("user2", persons.get( 1 ).getString( "name" ) );
        assertEquals("pass2", persons.get( 1 ).getString( "password" ) );
        assertEquals("email2@mail.ru", persons.get( 1 ).getString( "email" ) );
    }

    @Test
    public void testListCached()
    {
        Cache<Object, Object> test = be5Caches.getCache( "TestCache" );

        long hitCount = test.stats().hitCount();

        List<QRec> persons = db.list( "SELECT * FROM persons ORDER BY name", "TestCache" );

        assertEquals( hitCount, test.stats().hitCount() );

        assertEquals( 2, persons.size() );

        assertEquals("user1", persons.get( 0 ).getString( "name" ) );
        assertEquals("pass1", persons.get( 0 ).getString( "password" ) );
        assertNull( persons.get( 0 ).getString( "email" ) );

        assertEquals("user2", persons.get( 1 ).getString( "name" ) );
        assertEquals("pass2", persons.get( 1 ).getString( "password" ) );
        assertEquals("email2@mail.ru", persons.get( 1 ).getString( "email" ) );

        db.list( "SELECT * FROM persons ORDER BY name", "TestCache" );

        assertEquals( hitCount + 1, test.stats().hitCount() );
    }
}
