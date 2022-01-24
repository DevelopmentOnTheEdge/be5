package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.query.sql.QRecParser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class QueriesServiceTest extends QueryBe5ProjectDBTest
{
    @Inject
    private QueriesService queries;

    @Before
    public void setUpTestUtils()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
        db.update("DELETE FROM testtableAdmin");
    }

    @Test
    public void getTagsFromEnum()
    {
        String[][] strings = new String[][]{{"Federal", "Федеральный"}, {"Municipal", "Муниципальный"}, {"Regional", "Региональный"}};

        String[][] tagsFromEnum = queries.getTagsFromEnum("testTags", "admlevel");
        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromEnumNoYes()
    {
        String[][] strings = new String[][]{{"no", "нет"}, {"yes", "да"}};

        String[][] tagsFromEnum = queries.getTagsFromEnum("testTags", "payable");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromSelectionView()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};

        String[][] tagsFromEnum = queries.getTagsFromSelectionView("testTags");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewTest()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};

        String[][] tagsFromEnum = queries.getTagsFromCustomSelectionView("testTags", "With parameter");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewCacheTest()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};

        String[][] tagsFromEnum = queries.getTagsFromSelectionView("testTags");
        assertArrayEquals(strings, tagsFromEnum);

        strings = new String[][]{{"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};
        tagsFromEnum = queries.getTagsFromCustomSelectionView("testTags", "*** Custom Selection view ***");
        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromOneColumnSelectionViewTest()
    {
        String[][] strings = new String[][]{
            {"Regional", "Региональный"},
            {"Municipal", "Муниципальный"},
            {"Federal", "Федеральный"},
            {"Regional", "Региональный"}
        };

        String[][] tagsFromEnum = queries.getTagsFromCustomSelectionView("testTags", "One More Selection view");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewWithParamTest()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}};

        String[][] tagsFromEnum = queries.getTagsFromCustomSelectionView("testTags", "With parameter",
                ImmutableMap.of("payable", "yes"));

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewWithParamTestNull()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};

        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("payable", null);

        String[][] tagsFromEnum = queries.getTagsFromCustomSelectionView("testTags", "With parameter",
                stringStringHashMap);

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromGroovyQuery()
    {
        String[][] tagsFromEnum = queries.getTagsFromCustomSelectionView("testtableAdmin", "TestGroovyTable");
        assertArrayEquals(new String[][]{{"a1", "b1"}, {"a2", "b2"}}, tagsFromEnum);
    }

    @Test
    public void getTagsYesNo()
    {
        String[][] strings = new String[][]{{"yes", "да"}, {"no", "нет"}};

        String[][] tagsFromEnum = queries.getTagsYesNo();

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsNoYes()
    {
        String[][] strings = new String[][]{{"no", "нет"}, {"yes", "да"}};

        String[][] tagsFromEnum = queries.getTagsNoYes();

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromQueryTest()
    {
        String[][] strings = new String[][]{{"01", "Regional"}, {"02", "Municipal"}, {"03", "Federal"}, {"04", "Regional"}};

        String[][] tagsFromEnum = queries.getTagsFromQuery("SELECT code AS \"CODE\", admlevel AS \"NAME\" FROM testTags");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromSQLFromFakeTable()
    {
        String[][] strings = {{"val1","name1"},{"val2","name2"},};
        String[][] tags = queries.getTagsFromQuery("SELECT 'val1','name1' UNION ALL SELECT 'val2','name2'");
        assertArrayEquals(strings, tags);
    }

    @Test
    public void getTagsFromSQLWithOneColumn()
    {
        String[][] strings = {{"yes","yes"},{"no","no"},};
        String[][] tags = queries.getTagsFromQuery("SELECT 'yes' UNION ALL SELECT 'no'");
        assertArrayEquals(strings, tags);
    }

    @Test
    public void localizeTags()
    {
        String[][] tags = queries.localizeTags("testTags", new String[][]{{"01", "Regional"}, {"02", "Municipal"}});

        assertArrayEquals(new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}},
                tags);
    }

    @Test
    public void localizeForQueryTags()
    {
        String[][] tags = queries.localizeTags("testTags", "All records", new String[][]{{"01", "Regional"}, {"02", "Municipal"}});

        assertArrayEquals(new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}},
                tags);
    }

    @Test
    public void localizeTagsMap()
    {
        String[][] tags = queries.localizeTags("testTags", ImmutableList.of(
                ImmutableList.of("01", "Regional"),
                ImmutableList.of("02", "Municipal")
        ));

        assertArrayEquals(new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}},
                tags);
    }

    @Test
    public void readAsMapTest()
    {
        Map<String, Object> values = queries.readAsMap("SELECT code AS \"CODE\", admlevel AS \"NAME\" FROM testTags");

        assertEquals(ImmutableMap.of(
                "01", "Regional",
                "02", "Municipal",
                "03", "Federal",
                "04", "Regional"), values);
    }

    @Test
    public void getTagsTest()
    {
        String[][] strings = new String[][]{{"01", "Региональный"}, {"02", "Муниципальный"}, {"03", "Федеральный"}, {"04", "Региональный"}};

        String[][] tagsFromEnum = queries.getTags("testTags", "code", "admlevel");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void readAsRecordsTest()
    {
        List<QRec> list = queries.readAsRecords("SELECT code, admlevel FROM testTags");

        assertEquals("01", list.get(0).getValue("code"));
        assertEquals("Regional", list.get(0).getString("admlevel"));

        assertEquals("02", list.get(1).getValue("code"));
        assertEquals("Municipal", list.get(1).getValue("admlevel"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsFromQueryTest()
    {
        List<QRec> list = queries.query("testTags", "With parameter", emptyMap());

        assertEquals("01", list.get(0).getValue("ID"));
        assertEquals("Regional", list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsWithLongFilter()
    {
        List<QRec> list = queries.query("filterTestTable", "Simple",
                singletonMap("ID", Collections.singletonList(123L)));
        assertEquals(0, list.size());
    }

    @Test
    public void readOneRecordTest()
    {
        QRec qRec = queries.queryRecord("testTags", "With parameter", emptyMap());

        assertEquals("01", qRec.getValue("ID"));
        assertEquals("Regional", qRec.getValue("Name"));
    }

    @Test
    public void readAsRecordsFromQuerySqlTest()
    {
        List<QRec> list = queries.query(
                "SELECT code AS \"ID\", admlevel AS \"NAME\"\n" +
                        "        FROM testTags\n" +
                        "        WHERE 1=1\n" +
                        "        <if parameter=\"payable\">\n" +
                        "          AND payable = <parameter:payable/>\n" +
                        "        </if>",
                emptyMap());

        assertEquals("01", list.get(0).getValue("ID"));
        assertEquals("Regional", list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsFromQuerySqlTestFilter()
    {
        List<QRec> list = queries.query("SELECT * FROM testTags",
                singletonMap("admlevel", "Federal"));

        assertEquals(1, list.size());
    }

    @Test
    public void testLocalization()
    {
        QRec list = queries.queryRecord("SELECT '{{{message}}}' AS \"test\" FROM testTags LIMIT 1", emptyMap());
        assertEquals("Сообщение", list.getString("test"));
    }

    @Test
    public void dynamicQuerySkipDictionaryLocalization()
    {
        QRec list = queries.queryRecord("SELECT '{{{testValue}}}' AS \"test\" FROM testTags LIMIT 1", emptyMap());
        assertEquals("testValue", list.getString("test"));
    }

    @Test
    public void readAsListTest()
    {
        List<List<Object>> lists = queries.listOfLists("SELECT code, admlevel FROM testTags");

        assertEquals("01", lists.get(0).get(0));
        assertEquals("Regional", lists.get(0).get(1));

        assertEquals("02", lists.get(1).get(0));
        assertEquals("Municipal", lists.get(1).get(1));

        assertEquals(4, lists.size());
    }

    @Test
    public void test()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "TestName", "1");

        QRec rec = queries.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id);

        Assert.assertNotNull(rec);
        assertEquals("TestName", rec.getProperty("name").getValue());
        assertEquals("TestName", rec.getValue("name"));
    }

    @Test
    public void testBeSql()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "1234567890", 1);

        assertEquals("10", queries.qRec("SELECT TO_CHAR(LENGTH(name)) FROM testtableAdmin WHERE id = ?", id).getValue());

        assertEquals("10", queries.qRec("SELECT CAST(LEN(name) AS VARCHAR) FROM testtableAdmin WHERE id = ?", id).getValue());
    }

    @Test
    public void testGetters()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "TestName", 123);

        QRec rec = queries.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id);

        if (rec != null)
        {
            //One request to the database for several fields
            assertEquals("TestName", rec.getString("name"));
            assertEquals(123, rec.getInt("valueCol"));
            assertEquals(123, rec.getLong("valueCol"));
        }


        //use db and DpsRecordAdapter.createDps
        assertEquals("TestName", db.oneString("SELECT name FROM testtableAdmin WHERE id = ?", id));
        assertEquals(123, (int) db.oneInteger("SELECT valueCol FROM testtableAdmin WHERE id = ?", id));

        QRec dps = db.select("SELECT * FROM testtableAdmin WHERE id = ?", new QRecParser(), id);
        if (dps != null)
        {
            assertEquals("TestName", dps.getValue("name"));
            assertEquals(123, Integer.parseInt(dps.getValue("valueCol").toString()));
        }

    }

    @Test
    public void testNullIfNoRecords()
    {
        assertEquals(null, queries.qRec("SELECT * FROM testtableAdmin WHERE name = ?", "not contain name"));
    }

}
