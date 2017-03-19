package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SqlServiceTest
{
    private static ProjectProviderImpl projectProvider;
    private static DatabaseService databaseService;
    private static SqlService db;

    @BeforeClass
    public static void setUpProjectProviderImpl() throws Exception
    {
        projectProvider = new ProjectProviderImpl(){
            @Override
            public Path getPath(ServletContext ctx, String attributeName) {
                if("be5.configPath".equals(attributeName) || "be5.projectSource".equals(attributeName))
                    return Paths.get("src/test/resources/app").toAbsolutePath();
                return Paths.get("");
            }
        };
        databaseService = new DatabaseServiceImpl(projectProvider);
        db = new SqlServiceImpl(databaseService);
    }

    @Test
    public void testDatabaseService() throws SQLException {
        db.update("DROP TABLE IF EXISTS Persons;" );
        db.update("CREATE TABLE Persons (\n" +
                "    ID  BIGSERIAL PRIMARY KEY,\n" +
                "    name varchar(255),\n" +
                "    password varchar(255),\n" +
                "    email varchar(255) \n" +
                ");");

        db.update("INSERT INTO Persons (name, password) VALUES (?,?)",
                "test1", "pass");

        long id = db.insert("INSERT INTO Persons (name, password) VALUES (?,?)", new ScalarHandler<Long>(),
                "test2", "pass");
        assertEquals(2L, id);

        List<String> strings = db.selectAll("select * from Persons", rs ->
            rs.getString("ID") + " "  + rs.getString("name") + " "
                    + rs.getString("password")
        );

        assertNotNull(strings);
        assertEquals("1 test1 pass", strings.get(1));
        assertEquals("2 test2 pass", strings.get(1));

        databaseService.getConnectionsStatistics();
    }

}
