package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class OperationHelperTest extends Be5ProjectDBTest
{
    @Inject private OperationHelper helper;

    private Request request;

    @Before
    public void before(){
        request = getMockRequest("");
    }

    @Test
    public void getTagsFromEnum()
    {
        String[][] strings = new String[][]{ {"Federal", "Федеральный"},{"Municipal", "Муниципальный"},{"Regional", "Региональный"} };

        String[][] tagsFromEnum = helper.getTagsFromEnum("testTags", "admlevel");
        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromEnumNoYes()
    {
        String[][] strings = new String[][]{ {"no", "нет"}, {"yes", "да"} };

        String[][] tagsFromEnum = helper.getTagsFromEnum("testTags", "payable");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromSelectionView()
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"},{"03", "Федеральный"}, {"04", "Региональный"} };

        String[][] tagsFromEnum = helper.getTagsFromSelectionView(request,"testTags");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewTest() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"},{"03", "Федеральный"}, {"04", "Региональный"} };

        String[][] tagsFromEnum = helper.getTagsFromCustomSelectionView(request,"testTags", "With parameter");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewWithParamTest() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"} };

        String[][] tagsFromEnum = helper.getTagsFromCustomSelectionView(request,"testTags", "With parameter",
                ImmutableMap.of("payable","yes"));

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromCustomSelectionViewWithParamTestNull() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"},{"03", "Федеральный"}, {"04", "Региональный"} };

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("payable", null);

        String[][] tagsFromEnum = helper.getTagsFromCustomSelectionView(request,"testTags", "With parameter",
                stringStringHashMap);

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsYesNo()
    {
        String[][] strings = new String[][]{ {"yes", "да"}, {"no", "нет"} };

        String[][] tagsFromEnum = helper.getTagsYesNo();

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsNoYes()
    {
        String[][] strings = new String[][]{ {"no", "нет"}, {"yes", "да"} };

        String[][] tagsFromEnum = helper.getTagsNoYes();

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromQueryTest() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Regional"},{"02", "Municipal"},{"03", "Federal"}, {"04", "Regional"} };

        String[][] tagsFromEnum = helper.getTagsFromQuery("SELECT code AS \"CODE\", admlevel AS \"NAME\" FROM testTags");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void localizeTags() throws Exception
    {
        String[][] tags = helper.localizeTags("testTags", new String[][]{ {"01", "Regional"},{"02", "Municipal"} });

        assertArrayEquals(new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"} },
                tags);
    }

    @Test
    public void localizeTagsMap() throws Exception
    {
        String[][] tags = helper.localizeTags("testTags", ImmutableList.of(
                ImmutableList.of("01", "Regional"),
                ImmutableList.of("02", "Municipal")
        ));

        assertArrayEquals(new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"} },
                tags);
    }

    @Test
    public void readAsMapTest() throws Exception
    {
        Map<String, String> values = helper.readAsMap("SELECT code AS \"CODE\", admlevel AS \"NAME\" FROM testTags");

        assertEquals(ImmutableMap.of(
                "01", "Regional",
                "02", "Municipal",
                "03", "Federal",
                "04", "Regional"), values);
    }

    @Test
    public void getTagsTest() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"},{"03", "Федеральный"}, {"04", "Региональный"} };

        String[][] tagsFromEnum = helper.getTags("testTags", "code", "admlevel");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void readAsRecordsTest() throws Exception
    {
        List<DynamicPropertySet> list = helper.readAsRecords("SELECT code, admlevel FROM testTags");

        assertEquals("01",        list.get(0).getValue("code"));
        assertEquals("Regional",  list.get(0).getValue("admlevel"));

        assertEquals("02",        list.get(1).getValue("code"));
        assertEquals("Municipal", list.get(1).getValue("admlevel"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsFromQueryTest() throws Exception
    {
        List<DynamicPropertySet> list = helper.readAsRecordsFromQuery("testTags", "With parameter",
                Collections.emptyMap(), request.getSession());

        assertEquals("01",        list.get(0).getValue("ID"));
        assertEquals("Regional",  list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsRecordsFromQuerySqlTest() throws Exception
    {
        List<DynamicPropertySet> list = helper.readAsRecordsFromQuery(
                        "SELECT code AS \"ID\", admlevel AS \"NAME\"\n" +
                        "        FROM testTags\n" +
                        "        WHERE 1=1\n" +
                        "        <if parameter=\"payable\">\n" +
                        "          AND payable = <parameter:payable/>\n" +
                        "        </if>",
                Collections.emptyMap(), request.getSession());

        assertEquals("01",        list.get(0).getValue("ID"));
        assertEquals("Regional",  list.get(0).getValue("Name"));

        assertEquals(4, list.size());
    }

    @Test
    public void readAsListTest() throws Exception
    {
        List<List<Object>> lists = helper.readAsList("SELECT code, admlevel FROM testTags");

        assertEquals("01",        lists.get(0).get(0));
        assertEquals("Regional",  lists.get(0).get(1));

        assertEquals("02",        lists.get(1).get(0));
        assertEquals("Municipal", lists.get(1).get(1));

        assertEquals(4, lists.size());
    }

}