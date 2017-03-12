package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.dbms.DbmsConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


public class MultiThreadedTest
{
    private static ProjectProviderImpl projectProvider = null;
    private static DatabaseService databaseService = null;

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
        assertNotNull(projectProvider);
        databaseService = new DatabaseServiceImpl(projectProvider);
        DbmsConnector conn = databaseService.getDbmsConnector();
        assertNotNull(conn);
        conn.executeUpdate("DROP TABLE IF EXISTS Persons;" );
        conn.executeUpdate("CREATE TABLE Persons (\n" +
                "    ID int NOT NULL AUTO_INCREMENT,\n" +
                "    name varchar(255),\n" +
                "    password varchar(255),\n" +
                "    email varchar(255) \n" +
                ");");
        conn.releaseConnection(conn.getConnection());
    }

    @Test
    public void testDatabaseService() throws SQLException {
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 100; i++) {
            service.execute(new Test2());
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DbmsConnector conn = databaseService.getDbmsConnector();
        ResultSet rs = conn.executeQuery("select count(*) AS \"count\" from Persons;");
        rs.next();
        int count = Integer.parseInt(rs.getString("count"));
        conn.close(rs);
        conn.releaseConnection(conn.getConnection());

        assertEquals(100, count);
    }

    public class Test2 implements Runnable{

        @Override
        public void run()  {
            try {
                DbmsConnector conn = databaseService.getDbmsConnector();

                SqlService db = new SqlServiceImpl(databaseService);
                Random random = new Random();

                conn.executeInsert("INSERT INTO Persons (name, password)" +
                        "VALUES ('test" + random.nextInt()%10 + "','" + random.nextInt() + "')");

                List<String> strings = db.from("Persons").selectAll(
                        rs -> rs.getString("ID") + " "
                                + rs.getString("name") + " " + rs.getString("password")
                );
                //System.out.println(strings.size() + " last: "+ strings.get(strings.size()-1));
                conn.releaseConnection(conn.getConnection());

                //System.out.println(databaseService.getConnectionsStatistics());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterClass
    public static void after(){
        while(databaseService.getNumActive() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(databaseService.getConnectionsStatistics());
    }

}
