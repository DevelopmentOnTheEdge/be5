package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;

import com.developmentontheedge.beans.DynamicPropertySet;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

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

}