package com.developmentontheedge.be5.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimes
{

    public static LocalDateTime fromNullableWithSystemDefaultZoneId(Timestamp date)
    {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }
    
}
