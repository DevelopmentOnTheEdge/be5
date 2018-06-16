package com.developmentontheedge.be5.server.util;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class DateUtilsTest
{
    @Test
    public void base()
    {
        assertEquals(DateUtils.makeDate( 2016, 12, 31 ),
                DateUtils.curMonthEnd(DateUtils.makeDate( 2016, 12, 15 )));
        assertEquals(DateUtils.makeDate( 2016, 12, 1 ),
                DateUtils.curMonthBegin(DateUtils.makeDate( 2016, 12, 15 )));

        assertEquals(DateUtils.makeDate( 2017, 1, 31 ),
                DateUtils.nextMonthEnd(DateUtils.makeDate( 2016, 12, 31 )));
        assertEquals(DateUtils.makeDate( 2017, 1, 1 ),
                DateUtils.nextMonthBegin(DateUtils.makeDate( 2016, 12, 31 )));

        assertEquals(DateUtils.makeDate( 2016, 11, 1 ),
                DateUtils.prevMonthBegin(DateUtils.makeDate( 2016, 12, 31 )));
        assertEquals(DateUtils.makeDate( 2016, 11, 30 ),
                DateUtils.prevMonthEnd(DateUtils.makeDate( 2016, 12, 31 )));

        assertEquals(DateUtils.makeDate( 2017, 1, 1 ),
                DateUtils.nextDay(DateUtils.makeDate( 2016, 12, 31 )));
        assertEquals(DateUtils.makeDate( 2016, 12, 30 ),
                DateUtils.prevDay(DateUtils.makeDate( 2016, 12, 31 )));

        Date date = DateUtils.makeDate(2016, 12, 1);

        assertEquals(date, DateUtils.toDate(new Timestamp(date.getTime())));
    }

    @Test
    public void curr()
    {
        Date date = DateUtils.makeDate(2016, 12, 1);

        assertEquals(1, DateUtils.currentDate().compareTo(date));

        assertEquals(1, DateUtils.curDay().compareTo(date));
        assertEquals(1, DateUtils.prevDay().compareTo(date));
        assertEquals(1, DateUtils.nextDay().compareTo(date));

        assertTrue(DateUtils.curMonth() <= 12);
        assertEquals(1, DateUtils.prevMonthEnd().compareTo(date));
        assertEquals(1, DateUtils.prevMonthBegin().compareTo(date));
        assertEquals(1, DateUtils.nextMonthEnd().compareTo(date));
        assertEquals(1, DateUtils.nextMonthBegin().compareTo(date));
    }

    @Test
    public void add()
    {
        Date date = DateUtils.makeDate(2016, 12, 1);

        assertEquals(DateUtils.nextDay(), DateUtils.addDays(DateUtils.prevDay(), 2));
        assertEquals(DateUtils.makeDate(2017, 1, 1), DateUtils.addMonths(date, 1));
    }

    @Test
    public void interval()
    {
        assertTrue(DateUtils.isActual(DateUtils.prevDay(), DateUtils.nextDay(), DateUtils.curDay()));
        assertTrue(DateUtils.isActual(DateUtils.curMonthBegin(), DateUtils.curMonthEnd(), DateUtils.curDay()));

        assertTrue(DateUtils.isIntersect(DateUtils.prevDay(), DateUtils.nextDay(), DateUtils.prevMonthBegin(), DateUtils.nextMonthBegin()));

        assertFalse(DateUtils.isIntersect(DateUtils.prevDay(), DateUtils.nextDay(), DateUtils.prevMonthBegin(), DateUtils.prevMonthEnd()));
    }

    @Test
    public void info()
    {
        Date date = DateUtils.makeDate(2016, 12, 20);
        assertEquals(31, DateUtils.daysInMonth(date));
        assertEquals("январь", DateUtils.monthNameRU(1));
        assertEquals("20 декабря 2016 г.", DateUtils.formatRussianCases(date));
        assertEquals("\"20\" декабря 2016 г.", DateUtils.formatRussianCasesQuotes(date));
        assertEquals("декабря 2016", DateUtils.formatRussianCasesPattern(date, "MMMM yyyy"));

        assertEquals("2016-12-20 00:00:00", DateUtils.toAnsiDateTime(new Timestamp(date.getTime())));

        assertEquals(2016, DateUtils.getYear(date));
        assertEquals(12, DateUtils.getMonth(date));
        assertEquals(20, DateUtils.getDay(date));
        assertEquals(0, DateUtils.getHours(date));
        assertEquals(0, DateUtils.getMinutes(date));

        assertEquals(DateUtils.makeDate(2016, 12, 19), DateUtils.curWeekMonday(date));
        assertEquals(DateUtils.makeDate(2016, 12, 23), DateUtils.curWeekFriday(date));
    }

    @Test
    public void diff()
    {
        assertEquals(30, DateUtils.getDaysDiff(DateUtils.makeDate( 2017, 1, 1 ),
                                                        DateUtils.makeDate( 2017, 1, 31 )));

        assertEquals(3, DateUtils.getMonthDiff(DateUtils.makeDate( 2017, 1, 15 ),
                DateUtils.makeDate( 2017, 4, 1 )));

        assertEquals(2, DateUtils.getFullMonthDiff(DateUtils.makeDate( 2017, 1, 15 ),
                DateUtils.makeDate( 2017, 4, 1 )));
    }

    @Test
    public void parse()
    {
        assertEquals(DateUtils.makeDate(2016, 12, 1), DateUtils.parse("DD/MM/yyyy", "1/12/2016"));
        assertEquals(DateUtils.makeDate(2016, 12, 1), DateUtils.parse("DD-MM-yyyy", "1-12-2016"));
    }

    @Test
    public void testIsBetween()
    {
        assertTrue( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
            DateUtils.makeDate( 2016, 12, 30 ), DateUtils.makeDate( 2017, 1, 1 ) ) );

        assertTrue( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
                DateUtils.makeDate( 2016, 12, 31 ), DateUtils.makeDate( 2017, 1, 1 ) ) );

        assertFalse( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
                DateUtils.makeDate( 2016, 12, 31 ), DateUtils.makeDate( 2016, 12, 31 ) ) );
    }

    @Test
    public void testAfter()
    {
        assertTrue( "2013-12-03", !DateUtils.makeDate(2013,12,3).after( DateUtils.makeDate(2013,12,5) ) );
        assertTrue( "2013-12-04", !DateUtils.makeDate(2013,12,4).after( DateUtils.makeDate(2013,12,5) ) );
        assertTrue( "2013-12-05", !DateUtils.makeDate(2013,12,5).after( DateUtils.makeDate(2013,12,5) ) );
    }

    @Test
    public void testSameDay()
    {
        assertTrue( DateUtils.isSameDay( DateUtils.makeDate( 2016, 12, 31 ), DateUtils.nextDay( DateUtils.makeDate( 2016, 12, 30 ) ) ) );
    }

    @Test
    public void isTime() throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        assertTrue(DateUtils.isTimeBefore(sdf.parse("2016.12.31 10:00:00"), "10:00:01"));
        assertTrue(DateUtils.isTimeAfter(sdf.parse("2016.12.31 10:00:01"), "10:00:00"));
    }
}
