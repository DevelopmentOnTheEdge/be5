package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


public class OperationHelperTest extends AbstractProjectTest
{
    private final static OperationHelper helper = injector.get(OperationHelper.class);

    @Test
    public void getTagsFromEnum() throws Exception
    {
        String[][] strings = new String[][]{ {"Federal", "Федеральный"},{"Municipal", "Муниципальный"},{"Regional", "Региональный"} };

        String[][] tagsFromEnum = helper.getTagsFromEnum("tagFromColumn", "admlevel");
        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsFromEnumNoYes() throws Exception
    {
        String[][] strings = new String[][]{ {"no", "нет"}, {"yes", "да"} };

        String[][] tagsFromEnum = helper.getTagsFromEnum("tagFromColumn", "payable");

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsYesNo() throws Exception
    {
        String[][] strings = new String[][]{ {"yes", "да"}, {"no", "нет"} };

        String[][] tagsFromEnum = helper.getTagsYesNo();

        assertArrayEquals(strings, tagsFromEnum);
    }

    @Test
    public void getTagsNoYes() throws Exception
    {
        String[][] strings = new String[][]{ {"no", "нет"}, {"yes", "да"} };

        String[][] tagsFromEnum = helper.getTagsNoYes();

        assertArrayEquals(strings, tagsFromEnum);
    }
}