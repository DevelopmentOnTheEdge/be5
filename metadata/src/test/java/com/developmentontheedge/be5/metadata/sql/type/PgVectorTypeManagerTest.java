package com.developmentontheedge.be5.metadata.sql.type;

import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.IndexColumnDef;
import com.developmentontheedge.be5.metadata.model.IndexDef;
import com.developmentontheedge.be5.metadata.model.TableDef;
import com.developmentontheedge.be5.metadata.sql.Rdbms;
import com.developmentontheedge.dbms.ExtendedSqlException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PgVectorTypeManagerTest extends BaseTypeManagerTest
{
    private TableDef createChunks(Rdbms dbms, String vectorType)
    {
        TableDef def = createTable(dbms);
        addColumn(def, "doc_id", "VARCHAR(100)");
        ColumnDef embedding = addColumn(def, "embedding", vectorType);
        embedding.setCanBeNull(true);
        return def;
    }

    private IndexDef addIndex(TableDef def, String name, String column)
    {
        IndexDef idx = new IndexDef(name, def.getIndices());
        DataElementUtils.save(idx);
        DataElementUtils.save(new IndexColumnDef(column, idx));
        return idx;
    }

    private IndexDef addHnswIndex(TableDef def)
    {
        IndexDef idx = addIndex(def, "table_embedding_idx", "embedding");
        idx.setMethod("hnsw");
        idx.setOperatorClass("vector_cosine_ops");
        idx.setOptions("m = 16, ef_construction = '64'");
        return idx;
    }

    @Test
    public void createTablePostgres()
    {
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(768)");
        addHnswIndex(def);
        addIndex(def, "table_doc_id_idx", "doc_id");
        assertEquals("DROP TABLE IF EXISTS \"table\";\n" +
                "CREATE EXTENSION IF NOT EXISTS vector;\n" +
                "CREATE TABLE \"table\" (\n" +
                "doc_id VARCHAR(100) NOT NULL,\n" +
                "embedding VECTOR(768));\n" +
                "CREATE INDEX table_doc_id_idx ON \"table\"(doc_id);\n" +
                "CREATE INDEX table_embedding_idx ON \"table\" USING HNSW (embedding vector_cosine_ops)" +
                " WITH (m=16, ef_construction=64);\n", def.getDdl());
    }

    @Test
    public void ivfflatIndexWithoutOptions()
    {
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(3)");
        IndexDef idx = addIndex(def, "table_embedding_idx", "embedding");
        idx.setMethod("IVFFLAT");
        assertEquals("CREATE INDEX table_embedding_idx ON \"table\" USING IVFFLAT (embedding);", idx.getCreateDdl());
        idx.setMethod("btree");
        assertEquals("", idx.getMethod());
        assertEquals("CREATE INDEX table_embedding_idx ON \"table\"(embedding);", idx.getCreateDdl());
    }

    @Test
    public void addVectorColumn() throws ExtendedSqlException
    {
        TableDef old = createTable(Rdbms.POSTGRESQL);
        addColumn(old, "doc_id", "VARCHAR(100)");
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(768)");
        assertEquals("CREATE EXTENSION IF NOT EXISTS vector;\n" +
                "ALTER TABLE \"table\" ADD COLUMN embedding VECTOR(768);\n", def.getDiffDdl(old, null));
    }

    @Test
    public void notNullVectorColumnIsAddedWithoutDefault() throws ExtendedSqlException
    {
        TableDef old = createTable(Rdbms.POSTGRESQL);
        addColumn(old, "doc_id", "VARCHAR(100)");
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(3)");
        def.getColumns().get("embedding").setCanBeNull(false);
        assertEquals("CREATE EXTENSION IF NOT EXISTS vector;\n" +
                "ALTER TABLE \"table\" ADD COLUMN embedding VECTOR(3) NOT NULL;\n", def.getDiffDdl(old, null));
    }

    @Test
    public void sameSchemeHasNoDiff() throws ExtendedSqlException
    {
        TableDef old = createChunks(Rdbms.POSTGRESQL, "vector(768)");
        addHnswIndex(old);
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(768)");
        addHnswIndex(def);
        assertEquals("", def.getDiffDdl(old, null));
        assertEquals("", def.getDangerousDiffStatements(old, null));
    }

    @Test
    public void changeDimensionsAltersColumn() throws ExtendedSqlException
    {
        TableDef old = createChunks(Rdbms.POSTGRESQL, "VECTOR(768)");
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(1024)");
        assertEquals("ALTER TABLE \"table\" ALTER COLUMN embedding SET DATA TYPE VECTOR(1024);\n",
                def.getDiffDdl(old, null));
        assertEquals("", def.getDangerousDiffStatements(old, null));
    }

    @Test
    public void changeIndexOperatorClassRecreatesIndex() throws ExtendedSqlException
    {
        TableDef old = createChunks(Rdbms.POSTGRESQL, "VECTOR(3)");
        addHnswIndex(old);
        TableDef def = createChunks(Rdbms.POSTGRESQL, "VECTOR(3)");
        addHnswIndex(def).setOperatorClass("vector_l2_ops");
        assertEquals("DROP INDEX IF EXISTS table_embedding_idx;\n" +
                "CREATE INDEX table_embedding_idx ON \"table\" USING HNSW (embedding vector_l2_ops)" +
                " WITH (m=16, ef_construction=64);", def.getDiffDdl(old, null));
    }

    @Test
    public void vectorIsTextInOtherDbms()
    {
        TableDef def = createChunks(Rdbms.H2, "VECTOR(768)");
        addHnswIndex(def);
        assertEquals("DROP TABLE IF EXISTS \"table\";\n" +
                "CREATE TABLE \"table\" (\n" +
                "doc_id VARCHAR(100) NOT NULL,\n" +
                "embedding TEXT);\n" +
                "CREATE INDEX table_embedding_idx ON \"table\"(embedding);\n", def.getDdl());

        assertEquals("TEXT", Rdbms.MYSQL.getTypeManager().getTypeClause(
                createChunks(Rdbms.MYSQL, "VECTOR(3)").getColumns().get("embedding").getType()));
    }
}
