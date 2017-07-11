package com.developmentontheedge.be5.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Deprecated
public class ResultSets
{
//
//    /**
//     * Parses a timestamp value as a local date time.
//     */
//    public static LocalDateTime getLocalDateTime(ResultSet rs, String fieldName) throws SQLException
//    {
//        return LocalDateTimes.fromNullableWithSystemDefaultZoneId(rs.getTimestamp(fieldName));
//    }
//
//    /**
//     * Parses a value of YesNo type as a boolean value.
//     */
//    public static boolean getBool(ResultSet rs, String columnLabel) throws SQLException
//    {
//        String string = rs.getString(columnLabel);
//
//        if (string == null)
//            return false;
//
//        if (string.equals("yes"))
//            return true;
//
//        if (string.equals("no"))
//            return false;
//
//        throw new IllegalStateException();
//    }
//
//    /**
//     * Parses a nano-representation of a duration.
//     */
//    public static Duration getDuration(ResultSet rs, String columnLabel) throws SQLException
//    {
//        return Duration.ofNanos(rs.getLong(columnLabel));
//    }
//
//    public static Long getNullableLong(ResultSet rs, String columnLabel) throws SQLException
//    {
//        long value = rs.getLong(columnLabel);
//
//        if (rs.wasNull())
//            return null;
//
//        return value;
//    }
//
//    public static Optional<Long> getOptionalLong(ResultSet rs, String columnLabel) throws SQLException
//    {
//        return Optional.ofNullable(getNullableLong(rs, columnLabel));
//    }
//
//    public static Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException
//    {
//        int value = rs.getInt(columnLabel);
//
//        if (rs.wasNull())
//            return null;
//
//        return value;
//    }
//
//    public static Optional<Integer> getOptionalInt(ResultSet rs, String columnLabel) throws SQLException
//    {
//        return Optional.ofNullable(getNullableInt(rs, columnLabel));
//    }
    
}
