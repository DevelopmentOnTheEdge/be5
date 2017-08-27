package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class OperationServiceImplTest extends AbstractProjectTest
{
    @Test
    public void selectedRowsTest(){
        assertTrue( Arrays.equals(new String[]{"1","2","3"}, OperationServiceImpl.selectedRows("1,2,3")));

        assertTrue( Arrays.equals(new String[]{"1"}, OperationServiceImpl.selectedRows("1")));

        assertTrue( Arrays.equals(new String[]{}, OperationServiceImpl.selectedRows("")));
    }

}