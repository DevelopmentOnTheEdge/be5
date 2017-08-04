package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.Arrays;


public class OperationHelperTest extends AbstractProjectTest
{
    @Test
    public void getTagsFromEnum() throws Exception
    {
        OperationHelper helper = injector.get(OperationHelper.class);
        String[][] tagsFromEnum = helper.getTagsFromEnum("tagFromColumn", "admlevel");
        for (int i = 0; i < tagsFromEnum.length; i++)
        {
            System.out.println(Arrays.toString(tagsFromEnum[i]));
        }

        //todo tagFromColumn payable
    }

}