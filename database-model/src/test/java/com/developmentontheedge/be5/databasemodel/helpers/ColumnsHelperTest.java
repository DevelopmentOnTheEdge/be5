package com.developmentontheedge.be5.databasemodel.helpers;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class ColumnsHelperTest
{
    @Test
    public void check_insert_columns_contain_all_update_columns()
    {
        assertTrue(ColumnsHelper.insertSpecialColumns.containsAll(ColumnsHelper.updateSpecialColumns));
    }
}