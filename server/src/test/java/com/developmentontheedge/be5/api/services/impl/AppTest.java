package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.dbms.DbmsConnector;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AppTest
{
    private static ProjectProviderImpl projectProvider;
    private static DatabaseService databaseService;

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
    }

    @Test
    public void testDatabaseService() throws SQLException {
        DbmsConnector conn = databaseService.getDbmsConnector();

        SqlService db = new SqlServiceImpl(databaseService);

        conn.executeUpdate("DROP TABLE IF EXISTS Persons;" );

        conn.executeUpdate("CREATE TABLE Persons (\n" +
            "    ID int NOT NULL AUTO_INCREMENT,\n" +
            "    name varchar(255),\n" +
            "    password varchar(255),\n" +
            "    email varchar(255) \n" +
            ");");

        conn.executeInsert("INSERT INTO Persons (name, password)" +
                                                  "VALUES ('test','pass')");

        List<String> strings = db.from("Persons").selectAll(
            rs -> rs.getString("ID") + " "
                    + rs.getString("name") + " " + rs.getString("password")
        );

        assertNotNull(strings);
        assertEquals("1 test pass", strings.get(0));

        databaseService.getConnectionsStatistics();
        conn.releaseConnection(conn.getConnection());
    }

}
