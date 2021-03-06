package com.developmentontheedge.be5.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest
{
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void inClause()
    {
        assertEquals("(?, ?, ?, ?, ?)", Utils.inClause(5));

        assertEquals("SELECT code FROM table WHERE id IN (?, ?, ?, ?, ?)",
                "SELECT code FROM table WHERE id IN " + Utils.inClause(5));
    }

    @Test
    public void addPrefix()
    {
        assertArrayEquals(new String[]{"companies.1", "companies.2"},
                Utils.addPrefix("companies.", new String[]{"1", "2"}));
    }

    @Test
    public void isEmptyTest()
    {
        assertTrue(Utils.isEmpty(null));
        assertTrue(Utils.isEmpty(""));
        assertTrue(Utils.isEmpty(new Object[]{}));
        assertTrue(Utils.isEmpty(new ArrayList()));
        assertTrue(Utils.isEmpty(new ArrayList<String>()));

        assertFalse(Utils.isEmpty(1));
        assertFalse(Utils.isEmpty("1"));
        assertFalse(Utils.isEmpty(new Object[]{1}));
        assertFalse(Utils.isEmpty(Collections.singletonList(1)));
        assertFalse(Utils.isEmpty(Collections.<String>singletonList("1")));
    }

    @Test
    public void isTrueValueParam()
    {
        assertTrue(Utils.isTrueValueParam("yes"));
        assertTrue(Utils.isTrueValueParam("1"));
        assertTrue(Utils.isTrueValueParam("on"));
        assertTrue(Utils.isTrueValueParam("TrUe"));

        assertFalse(Utils.isTrueValueParam(null));
        assertFalse(Utils.isTrueValueParam(""));
        assertFalse(Utils.isTrueValueParam("No"));
        assertFalse(Utils.isTrueValueParam("false"));

    }

    @Test
    public void requiredNotEmpty()
    {
        Utils.requireNonEmpty("1", "test message");
    }

    @Test
    public void requiredNotEmptyError()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("test message");
        Utils.requireNonEmpty(null, "test message");
    }

    @Test
    public void changeType()
    {
        assertEquals(3L, Utils.changeType("3", Long.class));
        assertEquals(3, Utils.changeType("3", Integer.class));
    }

    @Test
    public void changeTypeDateTime() throws ParseException
    {
        assertEquals(dateFormatter.parse("2017-08-27"), (java.sql.Date)Utils.changeType("2017-08-27", java.sql.Date.class));
        assertEquals(dateFormatter.parse("2017-08-27"), (Date)Utils.changeType("2017-08-27", Date.class));

        assertEquals(timeFormatter.parse("20:49:01"), (Time)Utils.changeType("20:49:01", Time.class));

        assertEquals(dateTimeFormatter.parse("2017-08-27 20:49:01"),
                (Timestamp)Utils.changeType("2017-08-27 20:49:01", Timestamp.class));

        java.sql.Date sqlDate = new java.sql.Date(dateTimeFormatter.parse("2017-08-27 20:49:01").getTime());
        Timestamp timestamp = (Timestamp)Utils.changeType(sqlDate, Timestamp.class);
        assertEquals(dateTimeFormatter.parse("2017-08-27 20:49:01"), timestamp);
    }

    @Test
    public void changeTypeArray()
    {
        String[] stringArray = new String[]{"1", "2", "3"};
        assertArrayEquals(new Long[]{1L, 2L, 3L}, (Long[]) Utils.changeType(stringArray, Long[].class));
        assertArrayEquals(new Integer[]{1, 2, 3}, (Integer[]) Utils.changeType(stringArray, Integer[].class));
    }

    @Test
    public void changeTypes()
    {
        assertArrayEquals(new Long[]{1L, 2L, 3L}, Utils.changeTypes(new String[]{"1", "2", "3"}, Long.class));

        assertArrayEquals(new String[]{"1", "2", "3"}, Utils.changeTypes(new Long[]{1L, 2L, 3L}, String.class));
    }

    @Test
    public void translit()
    {
        assertEquals("Incorrect transliteration", "russkij tekst", Utils.translit("русский текст"));
        assertEquals( "Incorrect transliteration",
                "abwgdejozijklmnoprstufhcchshshh_yxejuq", Utils.translit( "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" ) );
        assertEquals( "Incorrect transliteration",
                "ABWGDEJOZIJKLMNOPRSTUFHCCHSHSHH_YXEJUQ", Utils.translit( "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" ) );
    }

    @Test
    public void testRemoveBracketed()
    {
        String from = "\"<Hi<!--Some removed text--> there!>\"";
        String to = "\"<Hi there!>\"";
        assertEquals( to, Utils.removeBracketed( from, "<!--", "-->", null ) );

        from = "Обращения граждан<!--Reception-->";
        to = "Обращения граждан";
        assertEquals( to, Utils.removeBracketed( from, "<!--", "-->", null ) );
    }

    @Test
    public void testSafeXML()
    {
        String from = "\"< & >\"";
        String to = "&quot;&lt; &amp; &gt;&quot;";
        assertEquals( to, Utils.safeXML( from ) );
    }

    @Test
    public void testReplaceHTMLEntities()
    {
        String from = "&nbsp;&nbsp;&quot;&lt; &amp; &gt;&quot;";
        String to = "  \"< & >\"";
        assertEquals( to, Utils.replaceXmlEntities( from ) );
    }
}
