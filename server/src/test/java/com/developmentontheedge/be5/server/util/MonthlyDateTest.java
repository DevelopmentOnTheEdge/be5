package com.developmentontheedge.be5.server.util;

import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class MonthlyDateTest
{
    @Test
    public void base() throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        MonthlyDate date = new MonthlyDate("201806", "YYYYMM");
        MonthlyDate date2 = new MonthlyDate("2018-06", "YYYY-MM");
        MonthlyDate date3 = new MonthlyDate("2018.06", "YYYY.MM");
        assertEquals(date, date2);
        assertEquals(date, date3);

        assertTrue(date.lessThan(new MonthlyDate()));
        assertTrue(date.lessThan(new MonthlyDate(null)));
        assertTrue(date.lessThanOrEqual(new MonthlyDate()));
        assertTrue(date.lessOrEqual(java.sql.Date.valueOf(LocalDate.MIN)));

        assertEquals(new MonthlyDate(2099, 12), MonthlyDate.MAX_VALUE);
        assertEquals(new MonthlyDate(1900, 1), MonthlyDate.MIN_VALUE);

        assertEquals(new MonthlyDate(dateFormat.parse("2018-06-01")), date);

        assertEquals(date, MonthlyDate.min(date, new MonthlyDate()));

        assertEquals(new MonthlyDate(2099, 12), MonthlyDate.max(date, MonthlyDate.MAX_VALUE));
        assertEquals(MonthlyDate.MAX_VALUE, MonthlyDate.max(date, MonthlyDate.MAX_VALUE, MonthlyDate.MIN_VALUE));
    }

    @Test
    public void getCurrent() throws ParseException
    {
        MonthlyDate date = new MonthlyDate("2018.06", "YYYY.MM");

        assertEquals(1, date.getCurrentDay());
        assertEquals(6, date.getCurrentMonth());
        assertEquals(2018, date.getCurrentYear());
        assertEquals(30, date.getNumDays());
        assertEquals("2018-06-01", date.getDateStr(false));
        assertEquals("'2018-06-01'", date.getDateStr(true));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(new MonthlyDate(2018, 6), new MonthlyDate(dateFormat.parse("2018-06-16")).getFirstDay());
        assertEquals(new MonthlyDate(2018, 1), new MonthlyDate(dateFormat.parse("2018-06-16")).getFirstDayOfYear());
    }

    @Test
    public void increment()
    {
        MonthlyDate date = new MonthlyDate(2018, 6);

        assertEquals(7, date.increment().getCurrentMonth());
        assertEquals(8, date.increment(2).getCurrentMonth());
        assertEquals(5, date.decrement().getCurrentMonth());
        assertEquals(4, date.decrement(2).getCurrentMonth());

        assertEquals(new MonthlyDate(2018, 7), date.getNextMonth());
        assertEquals(new MonthlyDate(2018, 5), date.getPrevMonth());
        assertEquals(new MonthlyDate(2019, 6), date.getNextYear());
    }
}