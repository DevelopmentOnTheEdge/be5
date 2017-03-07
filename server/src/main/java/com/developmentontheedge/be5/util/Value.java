package com.developmentontheedge.be5.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Value
{
    static final DateTimeFormatter DATETIME = new DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_DATE).appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter();
    static final DateTimeFormatter DATE = DateTimeFormatter.ISO_DATE;
    
    public static LocalDateTime toLocalDateTime(Object val) {
        if(val instanceof LocalDateTime)
            return (LocalDateTime) val;
        if(val instanceof CharSequence)
            return LocalDateTime.parse((CharSequence) val, DATETIME);
        throw new IllegalArgumentException("Not a date-time: "+val);
    }
    
    public static LocalDate toLocalDate(Object val) {
        if(val instanceof LocalDate)
            return (LocalDate) val;
        if(val instanceof CharSequence)
            return LocalDate.parse((CharSequence) val, DATE);
        throw new IllegalArgumentException("Not a date: "+val);
    }
}
