package com.developmentontheedge.be5.metadata.sql.schema;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the SQL query built by {@link PostgresSchemaReader#buildReadColumnsQuery}.
 *
 * These tests verify the structural correctness of the query without requiring a
 * live PostgreSQL instance. The most important properties:
 *
 * 1. The join between check constraints and columns goes through
 *    pg_attribute.attnum, not information_schema.columns.ordinal_position.
 *    These values diverge after ALTER TABLE ... DROP COLUMN.
 *
 * 2. The query filters on contype = 'c' to select only CHECK constraints,
 *    which correctly excludes NOT NULL constraints on all supported versions
 *    (PG13-17: absent from pg_constraint; PG18+: contype = 'n').
 *
 * Real-database integration tests are in Postgres13SchemaReaderIntegrationTest
 * and Postgres18SchemaReaderIntegrationTest.
 */
public class PostgresSchemaReaderTest
{
    @Test
    public void queryJoinsThroughPgAttributeAttnum()
    {
        String query = PostgresSchemaReader.buildReadColumnsQuery("public");
        // conkey holds pg_attribute.attnum values — the join must use attnum
        assertTrue("Query must join pg_attribute with attnum = ANY(pc.conkey)",
                query.contains("pa.attnum = ANY(pc.conkey)"));
        // The old buggy form compared ordinal_position directly to conkey
        assertFalse("Query must NOT compare ordinal_position to conkey (diverges after DROP COLUMN)",
                query.contains("ordinal_position = ANY(pc.conkey)"));
        // Dropped columns must be excluded
        assertTrue("Query must filter out dropped attributes",
                query.contains("NOT pa.attisdropped"));
    }

    @Test
    public void queryFiltersOnContypeCheckOnly()
    {
        String query = PostgresSchemaReader.buildReadColumnsQuery("public");
        // contype = 'c' selects only CHECK constraints.
        // PG13-17: NOT NULL is not in pg_constraint at all.
        // PG18+:   NOT NULL gets contype = 'n', excluded by this filter.
        assertTrue("Query must filter on contype = 'c' to select only CHECK constraints",
                query.contains("pc.contype = 'c'"));
        // Must not use information_schema.check_constraints (PG18 includes NOT NULL there)
        assertFalse("Query must not use information_schema.check_constraints " +
                "(PG18 includes NOT NULL constraints there)",
                query.contains("information_schema.check_constraints"));
    }

    @Test
    public void queryBindsRelationByOid()
    {
        String query = PostgresSchemaReader.buildReadColumnsQuery("public");
        // The subquery must bind the relation by OID (pt.relname + nspname → oid),
        // not by constraint name alone (which can collide across schemas)
        assertTrue("Query must resolve the relation OID from pg_class + pg_namespace",
                query.contains("t.relname = c.table_name"));
        assertTrue("Query must resolve the schema via pg_namespace",
                query.contains("n.nspname = c.table_schema"));
        assertTrue("Query must bind conrelid to the resolved OID",
                query.contains("pc.conrelid ="));
    }

    @Test
    public void queryReturnsFirstCheckConstraintPerColumn()
    {
        String query = PostgresSchemaReader.buildReadColumnsQuery("public");
        // LIMIT 1 with ORDER BY pc.oid returns the oldest CHECK constraint for the column.
        // This matches the previous behaviour (one row per column, not multiple rows).
        assertTrue("Query must use LIMIT 1 to return at most one check clause per column",
                query.contains("LIMIT 1"));
        assertTrue("Query must use deterministic ordering (pc.oid) for the LIMIT",
                query.contains("ORDER BY pc.oid"));
    }

    @Test
    public void queryIncludesDefSchemaFilterWhenProvided()
    {
        String withSchema = PostgresSchemaReader.buildReadColumnsQuery("myschema");
        assertTrue("Query must filter by schema when defSchema is provided",
                withSchema.contains("c.table_schema='myschema'"));

        String noSchema = PostgresSchemaReader.buildReadColumnsQuery(null);
        assertFalse("Query must not include a schema filter when defSchema is null",
                noSchema.contains("c.table_schema="));
    }

    @Test
    public void queryDoesNotUseConstraintColumnNameJoins()
    {
        // The old implementation used 3 joins through information_schema
        // (constraint_column_usage, table_constraints, check_constraints)
        // matched on constraint_name, which is not unique across schemas.
        String query = PostgresSchemaReader.buildReadColumnsQuery("public");
        assertFalse("Query must not join via constraint_column_usage " +
                "(name-based matching is fragile)",
                query.contains("information_schema.constraint_column_usage"));
        assertFalse("Query must not join via table_constraints " +
                "(name-based matching is fragile)",
                query.contains("information_schema.table_constraints"));
    }
}
