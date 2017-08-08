package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


public class OperationHelperTest extends AbstractProjectIntegrationH2Test
{
    private final static OperationHelper helper = injector.get(OperationHelper.class);

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
        String[][] strings = new String[][]{ {"01", "Regional"},{"02", "Municipal"},{"03", "Federal"}, {"04", "Regional"} };

        String[][] tagsFromEnum = helper.getTagsFromSelectionView(getMockRequest(""),"testTags");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromQuery() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Regional"},{"02", "Municipal"},{"03", "Federal"}, {"04", "Regional"} };

        String[][] tagsFromEnum = helper.getTagsFromQuery(getMockRequest(""),"testTags", "With parameter");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromQueryWithParam() throws Exception
    {
        String[][] strings = new String[][]{ {"01", "Regional"},{"02", "Municipal"} };

        String[][] tagsFromEnum = helper.getTagsFromQuery(getMockRequest(""),"testTags", "With parameter",
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
}