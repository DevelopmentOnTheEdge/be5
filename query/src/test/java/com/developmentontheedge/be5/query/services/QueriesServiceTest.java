package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.sql.DpsRecordAdapter;
import com.developmentontheedge.beans.DynamicPropertySet;
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
        Map<String, String> values = queries.readAsMap("SELECT code AS \"CODE\", admlevel AS \"NAME\" FROM testTags");

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
        List<DynamicPropertySet> list = queries.readAsRecords("SELECT code, admlevel FROM testTags");

        assertEquals("01", list.get(0).getValue("code"));
        assertEquals("Regional", list.get(0).getValue("admlevel"));

        assertEquals("02", list.get(1).getValue("code"));
        assertEquals("Municipal", list.get(1).getValue("admlevel"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsFromQueryTest()
    {
        List<DynamicPropertySet> list = queries.readAsRecordsFromQuery("testTags", "With parameter",
                Collections.emptyMap());

        assertEquals("01", list.get(0).getValue("ID"));
        assertEquals("Regional", list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readOneRecordTest()
    {
        QRec qRec = queries.readOneRecord("testTags", "With parameter", Collections.emptyMap());

        assertEquals("01", qRec.getValue("ID"));
        assertEquals("Regional", qRec.getValue("Name"));
    }

    @Test
    public void readAsRecordsFromQuerySqlTest()
    {
        List<DynamicPropertySet> list = queries.readAsRecordsFromQuery(
                "SELECT code AS \"ID\", admlevel AS \"NAME\"\n" +
                        "        FROM testTags\n" +
                        "        WHERE 1=1\n" +
                        "        <if parameter=\"payable\">\n" +
                        "          AND payable = <parameter:payable/>\n" +
                        "        </if>",
                Collections.emptyMap());

        assertEquals("01", list.get(0).getValue("ID"));
        assertEquals("Regional", list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsListTest()
    {
        List<List<Object>> lists = queries.readAsList("SELECT code, admlevel FROM testTags");

        assertEquals("01", lists.get(0).get(0));
        assertEquals("Regional", lists.get(0).get(1));

        assertEquals("02", lists.get(1).get(0));
        assertEquals("Municipal", lists.get(1).get(1));

        assertEquals(4, lists.size());
    }

    @Test
    public void test()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)", "TestName", "1");

        QRec rec = queries.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id);

        Assert.assertNotNull(rec);
        assertEquals("TestName", rec.getProperty("name").getValue());
        assertEquals("TestName", rec.getValue("name"));
    }

    @Test
    public void testBeSql()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)", "1234567890", 1);

        assertEquals("10", queries.qRec("SELECT TO_CHAR(LENGTH(name)) FROM testtableAdmin WHERE id = ?", id).getValue());

        assertEquals("10", queries.qRec("SELECT CAST(LEN(name) AS VARCHAR) FROM testtableAdmin WHERE id = ?", id).getValue());
    }

    @Test
    public void testGetters()
    {
        Long id = db.insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)", "TestName", 123);

        QRec rec = queries.qRec("SELECT * FROM testtableAdmin WHERE id = ?", id);

        if (rec != null)
        {
            //One request to the database for several fields
            assertEquals("TestName", rec.getString("name"));
            assertEquals(123, rec.getInt("value"));
        }


        //use db and DpsRecordAdapter.createDps
        assertEquals("TestName", db.oneString("SELECT name FROM testtableAdmin WHERE id = ?", id));
        assertEquals(123, (int) db.oneInteger("SELECT value FROM testtableAdmin WHERE id = ?", id));

        DynamicPropertySet dps = db.select("SELECT * FROM testtableAdmin WHERE id = ?", DpsRecordAdapter::createDps, id);
        if (dps != null)
        {
            assertEquals("TestName", dps.getValue("name"));
            assertEquals(123, Integer.parseInt(dps.getValue("value").toString()));
        }

    }

    @Test
    public void testNullIfNoRecords()
    {
        assertEquals(null, queries.qRec("SELECT * FROM testtableAdmin WHERE name = ?", "not contain name"));
    }

}