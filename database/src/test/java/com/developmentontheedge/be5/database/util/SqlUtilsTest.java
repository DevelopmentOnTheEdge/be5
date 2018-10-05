package com.developmentontheedge.be5.database.util;

import com.developmentontheedge.be5.database.DatabaseTest;
import com.developmentontheedge.be5.database.impl.TestPerson;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class SqlUtilsTest extends DatabaseTest
{
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
    public void testResultSetParser()
    {
        TestPerson testPerson = db.select("SELECT * FROM persons WHERE name = ?", rs ->
                new TestPerson(
                        SqlUtils.getSqlValue(Long.class, rs, 1),
                        SqlUtils.getSqlValue(String.class, rs, 2),
                        SqlUtils.getSqlValue(String.class, rs, 3),
                        SqlUtils.getSqlValue(String.class, rs, 4)
                ), "user2");

        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test(expected = RuntimeException.class)
    public void testResultSetParserError()
    {
        TestPerson testPerson = db.select("SELECT * FROM persons WHERE name = ?", rs ->
                new TestPerson(
                        SqlUtils.getSqlValue(Long.class, rs, 1),
                        SqlUtils.getSqlValue(String.class, rs, 1),
                        SqlUtils.getSqlValue(String.class, rs, 3),
                        SqlUtils.getSqlValue(String.class, rs, 4)
                ), "user2");

        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void test()
    {
        assertEquals(Long.class, SqlUtils.getTypeClass(Types.BIGINT));
        assertEquals(Integer.class, SqlUtils.getTypeClass(Types.INTEGER));
        assertEquals(Short.class, SqlUtils.getTypeClass(Types.SMALLINT));

        assertEquals(Double.class, SqlUtils.getTypeClass(Types.DOUBLE));
        assertEquals(Double.class, SqlUtils.getTypeClass(Types.FLOAT));
        assertEquals(Double.class, SqlUtils.getTypeClass(Types.DECIMAL));
        assertEquals(Double.class, SqlUtils.getTypeClass(Types.REAL));
        assertEquals(Double.class, SqlUtils.getTypeClass(Types.NUMERIC));

        assertEquals(Boolean.class, SqlUtils.getTypeClass(Types.BOOLEAN));
        assertEquals(Date.class, SqlUtils.getTypeClass(Types.DATE));
        assertEquals(Time.class, SqlUtils.getTypeClass(Types.TIME));
        assertEquals(Timestamp.class, SqlUtils.getTypeClass(Types.TIMESTAMP));

        assertEquals(String.class, SqlUtils.getTypeClass(Types.VARCHAR));

    }
}
