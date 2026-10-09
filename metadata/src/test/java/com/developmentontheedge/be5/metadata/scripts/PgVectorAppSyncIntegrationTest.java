package com.developmentontheedge.be5.metadata.scripts;

import com.developmentontheedge.be5.metadata.JulLogConfigurator;
import com.developmentontheedge.be5.metadata.model.BeConnectionProfile;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.be5.metadata.sql.pojo.IndexInfo;
import com.developmentontheedge.be5.metadata.sql.pojo.SqlColumnInfo;
import com.developmentontheedge.be5.metadata.sql.schema.PostgresSchemaReader;
import com.developmentontheedge.be5.metadata.util.NullLogger;
import com.developmentontheedge.be5.metadata.util.ProjectTestUtils;
import com.developmentontheedge.dbms.DbmsType;
import com.developmentontheedge.dbms.SimpleConnector;
import com.developmentontheedge.dbms.SqlExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * be5:sync against a real PostgreSQL with pgvector extension.
 *
 * Verifies that an entity with a VECTOR column and an HNSW index is created by AppSync
 * and that the next sync finds the scheme up-to-date, so stored embeddings are preserved.
 */
public class PgVectorAppSyncIntegrationTest
{
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"));

    @BeforeClass
    public static void startPostgres() throws Exception
    {
        JulLogConfigurator.config();
        POSTGRES.start();
        execute("DROP TABLE IF EXISTS chunks");
    }

    @AfterClass
    public static void stopPostgres()
    {
        POSTGRES.stop();
    }

    @Test
    public void syncCreatesVectorTableAndKeepsItOnNextSync() throws Exception
    {
        List<String> firstSync = sync(createProject());
        assertTrue("Expected scheme to be created, but got: " + firstSync,
                !firstSync.contains("Database scheme is up-to-date"));
        execute("INSERT INTO chunks (doc_id, embedding) VALUES ('doc1', '[1,2,3]')");

        // HNSW index and vector dimensions are read back from the database
        Map<String, List<IndexInfo>> indices = schemaReader().readIndices(sqlExecutor(), "public", new NullLogger());
        IndexInfo hnsw = findIndex(indices.get("chunks"), "chunks_embedding_idx");
        assertNotNull(hnsw);
        assertEquals("hnsw", hnsw.getMethod());
        assertEquals("vector_cosine_ops", hnsw.getOperatorClass());
        assertEquals("m=16, ef_construction=64", hnsw.getOptions());
        Map<String, List<SqlColumnInfo>> columns = schemaReader().readColumns(sqlExecutor(), "public", new NullLogger());
        SqlColumnInfo embedding = findColumn(columns.get("chunks"), "embedding");
        assertNotNull(embedding);
        assertEquals("vector", embedding.getType());
        assertEquals(3, embedding.getSize());

        List<String> messages = sync(createProject());
        assertTrue("Expected up-to-date scheme, but got: " + messages,
                messages.contains("Database scheme is up-to-date"));
        assertEquals("[1,2,3]", queryString("SELECT embedding::text FROM chunks WHERE doc_id = 'doc1'"));
        assertEquals("doc1", queryString("SELECT doc_id FROM chunks ORDER BY embedding <=> '[1,2,3.1]' LIMIT 1"));
    }

    private static Project createProject()
    {
        Project project = ProjectTestUtils.getProject("test");
        Entity entity = ProjectTestUtils.createEntity(project, "chunks", "ID");
        TableDef scheme = new TableDef(entity);
        DataElementUtils.save(scheme);
        ColumnDef id = new ColumnDef("ID", scheme.getColumns());
        id.setTypeString("KEYTYPE");
        id.setAutoIncrement(true);
        id.setPrimaryKey(true);
        DataElementUtils.save(id);
        ColumnDef docId = new ColumnDef("doc_id", scheme.getColumns());
        docId.setTypeString("VARCHAR(100)");
        DataElementUtils.save(docId);
        ColumnDef embedding = new ColumnDef("embedding", scheme.getColumns());
        embedding.setTypeString("VECTOR(3)");
        embedding.setCanBeNull(true);
        DataElementUtils.save(embedding);

        IndexDef hnsw = new IndexDef("chunks_embedding_idx", scheme.getIndices());
        hnsw.setMethod("hnsw");
        hnsw.setOperatorClass("vector_cosine_ops");
        hnsw.setOptions("m = 16, ef_construction = 64");
        DataElementUtils.save(hnsw);
        DataElementUtils.save(new IndexColumnDef("embedding", hnsw));

        IndexDef docIdx = new IndexDef("chunks_doc_id_idx", scheme.getIndices());
        DataElementUtils.save(docIdx);
        DataElementUtils.save(new IndexColumnDef("doc_id", docIdx));

        BeConnectionProfile profile = new BeConnectionProfile("pgvector", project.getConnectionProfiles().getLocalProfiles());
        profile.setConnectionUrl(POSTGRES.getJdbcUrl());
        profile.setUsername(POSTGRES.getUsername());
        profile.setPassword(POSTGRES.getPassword());
        profile.setDriverDefinition(Rdbms.POSTGRESQL.getDriverDefinition());
        DataElementUtils.save(profile);
        return project;
    }

    private static List<String> sync(Project project)
    {
        List<String> messages = new ArrayList<>();
        new AppSync()
                .setBe5Project(project)
                .setProfileName("pgvector")
                .setForceUpdate(true)
                .setLogger(new NullLogger()
                {
                    @Override
                    public void info(String msg)
                    {
                        messages.add(msg);
                    }

                    @Override
                    public void error(String msg)
                    {
                        messages.add(msg);
                    }
                })
                .execute();
        return messages;
    }

    private static PostgresSchemaReader schemaReader()
    {
        return new PostgresSchemaReader();
    }

    private static SqlExecutor sqlExecutor() throws Exception
    {
        SimpleConnector connector = new SimpleConnector(DbmsType.POSTGRESQL, POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(), POSTGRES.getPassword());
        return new SqlExecutor(connector, SqlExecutor.class.getResource("basesql.properties"));
    }

    private static void execute(String sql) throws Exception
    {
        try (Connection conn = connect(); Statement st = conn.createStatement())
        {
            st.execute(sql);
        }
    }

    private static String queryString(String sql) throws Exception
    {
        try (Connection conn = connect(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private static Connection connect() throws Exception
    {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static IndexInfo findIndex(List<IndexInfo> indices, String name)
    {
        for (IndexInfo index : indices)
        {
            if (name.equals(index.getName()))
                return index;
        }
        return null;
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
