package com.developmentontheedge.be5.database.util;

import com.developmentontheedge.be5.database.QRec;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end regression test for unsigned integer column handling against a real
 * MySQL/MariaDB server.
 * <p>
 * The bug: an {@code INT UNSIGNED} column holding 2^32-1 (4294967295) overflows the signed
 * {@code int} read path. This test inserts the max unsigned 32-bit value, reads it back through
 * {@link QRec} (which uses {@link com.developmentontheedge.be5.database.adapters
 * .DpsRecordAdapter}), and asserts the full value survives.
 * <p>
 * <b>Opt-in / environment-driven.</b> The test is skipped ({@link Assume}) unless a MySQL server
 * is reachable. To run it, set the environment (or system) properties:
 * <ul>
 *   <li>{@code TEST_MYSQL_URL} — JDBC URL, e.g. {@code jdbc:mysql://localhost:3307/testdb}</li>
 *   <li>{@code TEST_MYSQL_USER} — default {@code root}</li>
 *   <li>{@code TEST_MYSQL_PASSWORD} — default {@code root}</li>
 * </ul>
 * A convenient way to start the server for CI:
 * <pre>
 * docker run -d --name be5-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=testdb -p 3307:3306 mysql:5
 * </pre>
 */
public class SqlUtilsUnsignedIntegrationTest
{
    private static final long MAX_UINT32 = 4294967295L; // 2^32 - 1

    private Connection connection;

    @Before
    public void setUp() throws Exception
    {
        String url = System.getenv("TEST_MYSQL_URL");
        if (url == null || url.isEmpty())
        {
            url = System.getProperty("TEST_MYSQL_URL");
        }
        Assume.assumeTrue("TEST_MYSQL_URL not set; skipping unsigned-int MySQL integration test",
                url != null && !url.isEmpty());

        String user = System.getenv("TEST_MYSQL_USER");
        if (user == null || user.isEmpty())
            user = System.getProperty("TEST_MYSQL_USER", "root");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        if (password == null || password.isEmpty())
            password = System.getProperty("TEST_MYSQL_PASSWORD", "root");

        String driver = url.startsWith("jdbc:mariadb") ? "org.mariadb.jdbc.Driver" : "com.mysql.jdbc.Driver";
        Class.forName(driver);
        connection = DriverManager.getConnection(url, user, password);
    }

    @Test
    public void readUnsignedIntMaxLosslessly() throws Exception
    {
        try (Statement st = connection.createStatement())
        {
            st.execute("DROP TABLE IF EXISTS unsigned_e2e");
            st.execute("CREATE TABLE unsigned_e2e (val INT UNSIGNED)");
            st.execute("INSERT INTO unsigned_e2e (val) VALUES (4294967295)");
        }
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT val FROM unsigned_e2e"))
        {
            assertTrue(rs.next());
            QRec rec = new QRec();
            com.developmentontheedge.be5.database.adapters.DpsRecordAdapter.addDp(rec, rs);
            // The value must come back as a Long with the full unsigned value, not a negative int.
            assertEquals(Long.valueOf(MAX_UINT32), rec.getLong());
            // And getLong() by column name also works.
            assertEquals(MAX_UINT32, rec.getLong("val"));
        }
        finally
        {
            try (Statement st = connection.createStatement())
            {
                st.execute("DROP TABLE IF EXISTS unsigned_e2e");
            }
            catch (Exception ignore)
            {
            }
        }
    }
}
