package com.developmentontheedge.be5.metadata.sql.schema;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PostgresSchemaReaderTest
{
    @Test
    public void testColumnsQueryIgnoresPostgres18NotNullConstraints()
    {
        String query = PostgresSchemaReader.getColumnsQuery("public");

        assertTrue(query.contains("pc.contype = 'c'"));
        assertFalse(query.contains("information_schema.check_constraints"));
        assertFalse(query.contains("information_schema.constraint_column_usage"));
    }

    @Test
    public void testColumnsQueryScopesChecksToColumnRelationAndSchema()
    {
        String query = PostgresSchemaReader.getColumnsQuery("public");

        assertTrue(query.contains("pt.relname = c.table_name"));
        assertTrue(query.contains("pn.nspname = c.table_schema"));
        assertTrue(query.contains("c.ordinal_position = ANY(pc.conkey)"));
        assertTrue(query.contains("AND c.table_schema='public'"));
    }
}
