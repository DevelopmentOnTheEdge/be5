package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.sql.ResultSetParser;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SqlServiceTest
{
    private static SqlService db;

    static final ResultSetParser<TestPerson> parser = rs ->
            new TestPerson(rs.getLong("id"),rs.getString("name"),
                    rs.getString("password"),rs.getString("email"));

    @BeforeClass
    public static void setUp()
    {
        ProjectProviderImpl projectProvider = new ProjectProviderImpl()
        {
            @Override
        	protected Path findProjectPath() 
            {
            	return Paths.get("src/test/resources/app").toAbsolutePath();
            }
        };
        
        DatabaseService databaseService = new DatabaseServiceImpl(projectProvider);
        db = new SqlServiceImpl(databaseService);
        db.update("DROP TABLE IF EXISTS persons;" );
        db.update("CREATE TABLE persons (\n" +
                "    ID  BIGSERIAL PRIMARY KEY,\n" +
                "    name varchar(255),\n" +
                "    password varchar(255),\n" +
                "    email varchar(255) \n" +
                ");");

        db.update("INSERT INTO persons (name, password) VALUES (?,?)",
                "user1", "pass1");
        db.insert("INSERT INTO persons (name, password, email) VALUES (?,?,?)",
                "user2", "pass2", "email2@mail.ru");
    }

    @Test
    public void testSelectScalar() {
        String password = db.selectScalar("SELECT password FROM persons WHERE name = ?", "user2");

        assertEquals("pass2", password);
    }

    @Test
    public void testCount() {
        long count = db.selectScalar("SELECT COUNT(id) FROM persons WHERE name = ?","notContainUser");

        assertEquals(0, count);
    }

    @Test
    public void testSelectString() {
        String password = db.selectScalar("SELECT password FROM persons WHERE name = ?",
                "user2");

        assertEquals("pass2", password);
    }

    @Test
    public void testGetNullIfNotContain() {
        Long id = db.selectScalar("SELECT id FROM persons WHERE name = ?", "notContainUser");

        assertEquals(null, id);
    }

    @Test
    public void testBeanHandlerSelect() {
        TestPerson testPerson = db.query("SELECT * FROM persons WHERE name = ?", TestPerson.beanHandler
                , "user2");

        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void testResultSetParser() {
        TestPerson testPerson = db.select("SELECT * FROM persons WHERE name = ?", parser
                , "user2");

        assertEquals("pass2", testPerson.getPassword());
        assertEquals("email2@mail.ru", testPerson.getEmail());
    }

    @Test
    public void testBeanHandlerGetNullIfNotContain() {
        TestPerson id = db.query("SELECT id FROM persons WHERE name = ?",
                TestPerson.beanHandler, "notContainUser");
        assertEquals(null, id);
    }

    @Test(expected = Be5Exception.class)
    public void testBeanHandlerError() {
        TestPerson id = db.query("SELECT id FROM2 persons WHERE name = ?",
                TestPerson.beanHandler, "notContainUser");
        assertEquals(null, id);
    }

    @Test
    public void testGetNullIfNotContainCustomObject() {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs ->
                rs.getString("email"), "notContainUser");
        assertEquals(null, email);
    }

    @Test
    public void testGetNullCustomObject() {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs ->
                rs.getString("email"), "user1");
        assertEquals(null, email);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNullCustomObjectErrorCallNext() {
        String email = db.select("SELECT email FROM persons WHERE name = ?", rs -> {
                rs.next();
                return rs.getString("email");
        }, "user1");
        assertEquals(null, email);
    }

    @Test
    public void testSelectListBeanHandler() {
        List<TestPerson> persons = db.query("SELECT * FROM persons", TestPerson.beanListHandler);

        assertTrue(persons.size() >= 2);
        assertEquals("pass1", persons.get(0).getPassword());
        assertEquals("email2@mail.ru", persons.get(1).getEmail());
    }

    @Test
    public void testSelectListResultSetParser() {
        List<TestPerson> persons = db.selectList("SELECT * FROM persons", parser);

        assertTrue(persons.size() >= 2);
        assertEquals("pass1", persons.get(0).getPassword());
        assertEquals("email2@mail.ru", persons.get(1).getEmail());
    }

    @Test
    public void testSelectListLambda() {
        List<String> strings = db.selectList("SELECT * FROM persons", rs ->
                rs.getString("ID") + " "  + rs.getString("name") + " "
                        + rs.getString("password")
        );

        assertTrue(strings.size() >= 2);
        assertEquals("1 user1 pass1", strings.get(0));
        assertEquals("2 user2 pass2", strings.get(1));
    }

    @Test
    public void testGetInsertId() {
        String uniqueName = "testGetInsertId";
        long id = db.insert("INSERT INTO persons (name, password) VALUES (?,?)",
                uniqueName, "pass");

        long idByName = db.selectScalar("SELECT ID FROM persons WHERE name = ?", uniqueName);

        assertEquals(idByName, id);
    }
}
