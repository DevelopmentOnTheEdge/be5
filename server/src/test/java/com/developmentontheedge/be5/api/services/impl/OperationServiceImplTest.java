package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class OperationServiceImplTest extends AbstractProjectTest{

    @Test
    public void selectedRowsTest(){
        OperationServiceImpl operationService = new OperationServiceImpl(injector);

        assertTrue( Arrays.equals(new Long[]{1L,2L,3L}, operationService.selectedRows("1,2,3")));

        assertTrue( Arrays.equals(new Long[]{1L}, operationService.selectedRows("1")));

        assertTrue( Arrays.equals(new Long[]{}, operationService.selectedRows("")));
    }


}