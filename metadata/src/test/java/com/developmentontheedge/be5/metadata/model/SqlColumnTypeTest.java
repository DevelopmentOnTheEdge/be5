package com.developmentontheedge.be5.metadata.model;

import org.junit.Test;

import static com.developmentontheedge.be5.metadata.model.SqlBoolColumnType.NO;
import static com.developmentontheedge.be5.metadata.model.SqlBoolColumnType.YES;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SqlColumnTypeTest
{
    @Test
    public void testCreate()
    {
        SqlColumnType varchar = new SqlColumnType("VARCHAR(20)");
        assertEquals("VARCHAR", varchar.getTypeName());
        assertEquals(20, varchar.getSize());
        assertFalse(varchar.isDateTime());
        assertArrayEquals(new String[0], varchar.getEnumValues());
        assertEquals("VARCHAR(20)", varchar.toString());
        varchar.setSize(30);
        assertEquals("VARCHAR(30)", varchar.toString());

        SqlColumnType enumType = new SqlColumnType("ENUM(a,'b',c)");
        assertEquals("ENUM", enumType.getTypeName());
        assertArrayEquals(new String[]{"a", "b", "c"}, enumType.getEnumValues());
        assertEquals("ENUM('a','b','c')", enumType.toString());
        enumType.setEnumValues(new String[]{"a", "b'c", "d"});
        assertEquals("ENUM('a','b''c','d')", enumType.toString());

        SqlColumnType decimal = new SqlColumnType("DECIMAL(10,1)");
        assertEquals("DECIMAL", decimal.getTypeName());
        assertEquals(10, decimal.getSize());
        assertEquals(1, decimal.getPrecision());
        assertEquals("DECIMAL(10,1)", decimal.toString());
        decimal.setSize(18);
        decimal.setPrecision(2);
        assertEquals("DECIMAL(18,2)", decimal.toString());

        SqlColumnType bool = new SqlColumnType("BOOL");
        assertEquals("BOOL", bool.getTypeName());
        assertArrayEquals(new String[]{NO, YES}, bool.getEnumValues());

        assertEquals("UNKNOWN", SqlColumnType.unknown().getTypeName());
        assertEquals("UNKNOWN", SqlColumnType.unknown().toString());

        SqlColumnType date = new SqlColumnType("DATE");
        assertTrue(date.isDateTime());
        assertTrue(date.isValid());

        SqlColumnType foo = new SqlColumnType("FOO");
        assertEquals("FOO", foo.getTypeName());
        assertFalse(foo.isValid());
        assertFalse(foo.doesSupportGeneratedKey());

        SqlColumnType currency = new SqlColumnType("CURRENCY");
        assertEquals(18, currency.getSize());
        assertEquals(2, currency.getPrecision());

        assertTrue(new SqlColumnType("INT").doesSupportGeneratedKey());
    }
}
