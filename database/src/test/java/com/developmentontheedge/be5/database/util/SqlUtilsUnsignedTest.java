package com.developmentontheedge.be5.database.util;

import com.developmentontheedge.be5.database.DatabaseTest;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the driver-agnostic parts of the unsigned-integer fix.
 * <p>
 * The end-to-end unsigned-int test (a real MySQL/MariaDB with an {@code INT UNSIGNED} column
 * holding 2^32-1) lives in {@link SqlUtilsUnsignedIntegrationTest}, which runs against a
 * container and is skipped when the DB is unavailable.
 */
public class SqlUtilsUnsignedTest extends DatabaseTest
{
    private static final long MAX_UINT32 = 4294967295L; // 2^32 - 1

    /**
     * When the driver returns a {@link Long} for a value that fits in an {@code int}, the
     * {@code Integer.class} mapping still narrows correctly.
     */
    @Test
    public void getSqlValueIntegerClassNarrowsWhenFits()
    {
        assertEquals(Integer.valueOf(42), SqlUtils.getSqlValue(Integer.class, longRs(42), 1));
    }

    /**
     * When the driver returns a value beyond {@link Integer#MAX_VALUE}, the {@code Integer.class}
     * mapping must fail with a clear message, not silently wrap to a negative number.
     */
    @Test
    public void getSqlValueIntegerClassRejectsOverflow()
    {
        RuntimeException caught = null;
        try
        {
            SqlUtils.getSqlValue(Integer.class, longRs(MAX_UINT32), 1);
        }
        catch (RuntimeException e)
        {
            caught = e;
        }
        assertTrue("Expected RuntimeException for overflow, got: " + caught, caught != null);
        // The outer message names the column; the cause carries the overflow explanation.
        String all = caught.getMessage() + " / cause: "
                + (caught.getCause() == null ? null : caught.getCause().getMessage());
        assertTrue("Message should explain the overflow, got: " + all, all.contains("exceeds the range"));
    }

    /**
     * When the driver returns a {@link BigInteger} (modern Connector/J 8 unsigned representation),
     * the {@code Long.class} mapping reads the full value.
     */
    @Test
    public void getSqlValueLongClassFromBigInteger()
    {
        assertEquals(Long.valueOf(MAX_UINT32),
                SqlUtils.getSqlValue(Long.class, bigIntRs(MAX_UINT32), 1));
    }

    /**
     * When the driver returns a {@link Long} equal to 2^32-1, the {@code Long.class} mapping reads
     * the full value (no signed wrap).
     */
    @Test
    public void getSqlValueLongClassFromLong()
    {
        assertEquals(Long.valueOf(MAX_UINT32),
                SqlUtils.getSqlValue(Long.class, longRs(MAX_UINT32), 1));
    }

    /**
     * {@link SqlUtils#getTypeClass(ResultSetMetaData, int)} must map an unsigned integer column
     * (type name ending in "UNSIGNED") to {@link Long}.
     */
    @Test
    public void getTypeClassMapsUnsignedIntegerToLong()
    {
        assertEquals(Long.class, SqlUtils.typeClassForJdbcType(java.sql.Types.INTEGER, "INT UNSIGNED"));
    }

    @Test
    public void getTypeClassMapsSignedIntegerToInteger()
    {
        assertEquals(Integer.class, SqlUtils.typeClassForJdbcType(java.sql.Types.INTEGER, "INT"));
    }

    // ---- helpers ---------------------------------------------------------

    private static ResultSet longRs(long value)
    {
        return proxyRs(Long.valueOf(value));
    }

    private static ResultSet bigIntRs(long value)
    {
        return proxyRs(new BigInteger(Long.toString(value)));
    }

    private static ResultSet proxyRs(final Object value)
    {
        final ResultSetMetaData meta = (ResultSetMetaData) java.lang.reflect.Proxy.newProxyInstance(
                SqlUtilsUnsignedTest.class.getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                (proxy, method, args) -> "getColumnName".equals(method.getName()) ? "c" : null);
        return (ResultSet) java.lang.reflect.Proxy.newProxyInstance(
                SqlUtilsUnsignedTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) ->
                {
                    String name = method.getName();
                    if ("getObject".equals(name))
                        return value;
                    if ("getMetaData".equals(name))
                        return meta;
                    return null;
                });
    }

}
