package com.developmentontheedge.be5.util;

import com.developmentontheedge.be5.server.util.DateUtils;
import junit.framework.TestCase;


public class DateUtilsTest extends TestCase
{
    public void testIsBetween()
    {
        assertTrue( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
            DateUtils.makeDate( 2016, 12, 30 ), DateUtils.makeDate( 2017, 1, 1 ) ) );
    }

    public void testIsBetween1()
    {
        assertTrue( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
            DateUtils.makeDate( 2016, 12, 31 ), DateUtils.makeDate( 2017, 1, 1 ) ) );
    }

    public void testIsBetween2()
    {
        assertFalse( DateUtils.isBetween( DateUtils.makeDate( 2016, 12, 31 ),
            DateUtils.makeDate( 2016, 12, 31 ), DateUtils.makeDate( 2016, 12, 31 ) ) );
    }

    public void testAfter()
    {
        assertTrue( "2013-12-03", !DateUtils.makeDate(2013,12,3).after( DateUtils.makeDate(2013,12,5) ) );
        assertTrue( "2013-12-04", !DateUtils.makeDate(2013,12,4).after( DateUtils.makeDate(2013,12,5) ) );
        assertTrue( "2013-12-05", !DateUtils.makeDate(2013,12,5).after( DateUtils.makeDate(2013,12,5) ) );
    }

    public void testSameDay()
    {
        assertTrue( DateUtils.isSameDay( DateUtils.makeDate( 2016, 12, 31 ), DateUtils.nextDay( DateUtils.makeDate( 2016, 12, 30 ) ) ) );
    }
}
