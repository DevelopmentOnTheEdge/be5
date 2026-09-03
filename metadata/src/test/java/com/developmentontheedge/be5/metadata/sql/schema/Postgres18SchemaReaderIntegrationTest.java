package com.developmentontheedge.be5.metadata.sql.schema;

import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.dbms.DbmsType;
import com.developmentontheedge.dbms.SimpleConnector;
import com.developmentontheedge.dbms.SqlExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Integration test against a real PostgreSQL 18 container.
 *
 * PG18 changed NOT NULL representation: NOT NULL constraints are now stored
 * in pg_constraint with contype = 'n'. The query in PostgresSchemaReader
 * filters on contype = 'c', which must correctly exclude these new entries.
 *
 * Verifies the same three properties as Postgres13SchemaReaderIntegrationTest:
 * <ul>
 *   <li>CHECK constraints on columns with divergent attnum/ordinal_position
 *       are still detected after ALTER TABLE ... DROP COLUMN.</li>
 *   <li>NOT NULL constraints do not appear as check clauses.</li>
 *   <li>Regular CHECK constraints are reported.</li>
 * </ul>
 */
public class Postgres18SchemaReaderIntegrationTest
{
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18").withReuse(true);

    @BeforeClass
    public static void startPostgresAndCreateFixture() throws Exception
    {
        POSTGRES.start();
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement st = conn.createStatement())
        {
            st.execute("DROP TABLE IF EXISTS check_target");
            st.execute(
                    "CREATE TABLE check_target (" +
                    "  a integer, " +
                    "  b integer NOT NULL, " +
                    "  c varchar(10) NOT NULL CHECK (c IN ('a', 'b')), " +
                    "  d integer" +
                    ")");
            st.execute("ALTER TABLE check_target DROP COLUMN a");
        }
    }

    @AfterClass
    public static void stopPostgres()
    {
        POSTGRES.stop();
    }

    /**
     * After DROP COLUMN a:
     *   b -> attnum=2, ordinal_position=1
     *   c -> attnum=3, ordinal_position=2  (diverges from old query)
     *   d -> attnum=4, ordinal_position=3
     *
     * The CHECK on c must still be detected even though its ordinal_position (2)
     * does not match its attnum (3).
     */
    @Test
    public void checkConstraintOnColumnAfterDropColumnIsDetected() throws Exception
    {
        Map<String, List<SqlColumnInfo>> result = readColumns();
        List<SqlColumnInfo> cols = result.get("check_target");
        assertNotNull("Table check_target should be present", cols);

        SqlColumnInfo c = findColumn(cols, "c");
        assertNotNull("Column c should be present", c);
        assertNotNull("CHECK constraint on c should be detected via attnum join", c.getEnumValues());
        assertArrayEquals("Enum values extracted from CHECK (c IN ('a','b'))",
                new String[]{"a", "b"}, c.getEnumValues());
    }

    /**
     * NOT NULL on column b must not appear as a check_clause.
     * On PG18, NOT NULL is stored in pg_constraint with contype = 'n';
     * the contype = 'c' filter must exclude it.
     */
    @Test
    public void notNullConstraintIsNotReportedAsCheckClause() throws Exception
    {
        Map<String, List<SqlColumnInfo>> result = readColumns();
        List<SqlColumnInfo> cols = result.get("check_target");
        assertNotNull(cols);

        SqlColumnInfo b = findColumn(cols, "b");
        assertNotNull("Column b should be present", b);
        assertFalse("Column b is NOT NULL", b.isCanBeNull());
        assertNull("NOT NULL must not appear as a check clause / enum values on PG18", b.getEnumValues());
    }

    /**
     * The CHECK constraint should be reported for column c (regression guard
     * for the case where the fix accidentally removes all check clauses).
     */
    @Test
    public void regularCheckConstraintIsReported() throws Exception
    {
        Map<String, List<SqlColumnInfo>> result = readColumns();
        List<SqlColumnInfo> cols = result.get("check_target");
        assertNotNull(cols);

        SqlColumnInfo c = findColumn(cols, "c");
        assertNotNull("Column c should be present", c);
        assertNotNull("CHECK constraint should be present for column c", c.getEnumValues());
        assertEquals(2, c.getEnumValues().length);
    }

    private static Map<String, List<SqlColumnInfo>> readColumns() throws Exception
    {
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()))
        {
            SimpleConnector connector = new SimpleConnector(DbmsType.POSTGRESQL,
                    POSTGRES.getJdbcUrl(), conn);
            SqlExecutor sql = new SqlExecutor(connector,
                    SqlExecutor.class.getResource("basesql.properties"));
            return new PostgresSchemaReader().readColumns(sql, "public", new NullLogger());
        }
    }

    private static SqlColumnInfo findColumn(List<SqlColumnInfo> cols, String name)
    {
        for (SqlColumnInfo col : cols)
        {
            if (name.equals(col.getName()))
                return col;
        }
        return null;
    }
}
