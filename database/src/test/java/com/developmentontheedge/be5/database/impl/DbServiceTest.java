package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.test.DatabaseTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class DbServiceTest extends DatabaseTest
{
    private static final ResultSetParser<TestPerson> parser = rs ->
            new TestPerson(rs.getLong("id"), rs.getString("name"),
                    rs.getString("password"), rs.getString("email"));

    @Before
    public void setUp()
    {
        db.update("DELETE FROM persons");

        int update = db.update("INSERT INTO persons (name, password) VALUES (?,?)",
                "user1", "pass1");
        assertEquals(1, update);
        db.insert("INSERT INTO persons (name, password, email) VALUES (?,?,?)",
                "user2", "pass2", "email2@mail.ru");
    }

    @Test
    public void updateUnsafe()
    {
        int count = db.updateUnsafe("ALTER TABLE persons ADD column_test varchar(40)");

        assertEquals(0, count);
        assertNull(db.one("select column_test from persons"));
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
        assertEquals((Long) 0L, db.oneLong("SELECT COUNT(id) FROM persons WHERE name = ?", "notContainUser"));
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

        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void testResultSetParser()
    {
        TestPerson testPerson = db.select("SELECT * FROM persons WHERE name = ?", parser
                , "user2");

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
}
