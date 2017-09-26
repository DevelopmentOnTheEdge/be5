package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


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
    public void getTagsTest() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Региональный"},{"02", "Муниципальный"},{"03", "Федеральный"}, {"04", "Региональный"} };

        String[][] tagsFromEnum = helper.getTags("testTags", "code", "admlevel");

        assertArrayEquals(strings, tagsFromEnum);
    }
}