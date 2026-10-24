package com.developmentontheedge.be5.metadata.sql.schema;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the default value normalization in {@link MySqlSchemaReader}.
 *
 * MySQL 5.7 returns defaults from information_schema.columns without quotes,
 * while MySQL 8 / MariaDB return them quoted. Both must be normalized to the
 * quoted form used by the DDL generation. A SQL NULL default is returned by
 * both as the literal string NULL and must be converted to null (no default),
 * not to the string literal 'NULL' which causes a false DDL diff on every
 * be5:sync against MySQL / MariaDB.
 */
public class MySqlSchemaReaderTest
{
    @Test
    public void nullDefaultIsKeptAsNull()
    {
        assertNull(MySqlSchemaReader.normalizeDefaultValue(null, "varchar(100)"));
    }

    @Test
    public void sqlNullDefaultIsNotTreatedAsStringLiteral()
    {
        // MySQL 5.7: DEFAULT NULL -> information_schema columns.column_default = 'NULL'
        assertNull(MySqlSchemaReader.normalizeDefaultValue("NULL", "varchar(100)"));
        assertNull(MySqlSchemaReader.normalizeDefaultValue("NULL", "datetime"));
        assertNull(MySqlSchemaReader.normalizeDefaultValue("NULL", "int(11)"));
        assertNull(MySqlSchemaReader.normalizeDefaultValue("NULL", "text"));
    }

    @Test
    public void stringDefaultFromMySql57IsQuoted()
    {
        assertEquals("'no'", MySqlSchemaReader.normalizeDefaultValue("no", "varchar(3)"));
        assertEquals("'yes'", MySqlSchemaReader.normalizeDefaultValue("yes", "enum('no','yes')"));
        assertEquals("''", MySqlSchemaReader.normalizeDefaultValue("", "varchar(10)"));
    }

    @Test
    public void stringDefaultFromMySql8IsNotDoubled()
    {
        assertEquals("'no'", MySqlSchemaReader.normalizeDefaultValue("'no'", "varchar(3)"));
    }

    @Test
    public void dateDefaultIsNormalized()
    {
        assertEquals("NOW()", MySqlSchemaReader.normalizeDefaultValue("CURRENT_TIMESTAMP", "datetime"));
        assertEquals("NOW()", MySqlSchemaReader.normalizeDefaultValue("current_timestamp", "timestamp"));
        assertEquals("'2026-01-01'", MySqlSchemaReader.normalizeDefaultValue("2026-01-01", "date"));
        assertEquals("'2026-01-01'", MySqlSchemaReader.normalizeDefaultValue("'2026-01-01'", "date"));
    }

    @Test
    public void numericDefaultIsKeptAsIs()
    {
        assertEquals("0", MySqlSchemaReader.normalizeDefaultValue("0", "int(11)"));
        assertEquals("1.5", MySqlSchemaReader.normalizeDefaultValue("1.5", "double"));
    }
}
