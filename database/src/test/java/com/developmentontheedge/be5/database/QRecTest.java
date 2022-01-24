package com.developmentontheedge.be5.database;

import com.developmentontheedge.be5.util.DateUtils;
import com.developmentontheedge.beans.DynamicProperty;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class QRecTest
{
    @Test
    public void getDate()
    {
        QRec qRec = new QRec();
        qRec.add(new DynamicProperty("test", Date.class, DateUtils.makeDate(2018, 10, 27)));
        assertEquals("2018-10-27", qRec.getDate().toString());
        assertEquals("2018-10-27", qRec.getDate("test").toString());
    }

    @Test
    public void getBinaryStream() throws SQLException
    {
        String example = "This is an example";
        byte[] bytes = example.getBytes(UTF_8);
        QRec qRec = new QRec();
        qRec.add(new DynamicProperty("test", bytes.getClass(), bytes));
        assertEquals("This is an example", new BufferedReader(
                new InputStreamReader(qRec.getBinaryStream(), UTF_8)).lines()
                .collect(Collectors.joining("")));
        assertEquals("This is an example", new BufferedReader(
                new InputStreamReader(qRec.getBinaryStream("test"), UTF_8)).lines()
                .collect(Collectors.joining("")));
    }
}
