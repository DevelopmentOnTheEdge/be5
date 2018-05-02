package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.services.SqlService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SqlHelperTest
{
    private SqlService db;
    private SqlHelper sqlHelper;

    @Before
    public void setUp() throws Exception
    {
        db = mock(SqlService.class);
        sqlHelper = new SqlHelper(db);
    }

    @Test
    public void insert()
    {
        when(db.insert(any(), anyVararg())).thenReturn(2L);
        long newID = sqlHelper.insert("persons", ImmutableMap.of("name", "Test", "age", 30));

        assertEquals(2L, newID);
        verify(db, times(1)).insert("INSERT INTO persons (name, age) VALUES (?, ?)", "Test", 30);
    }

    @Test
    public void update()
    {
        when(db.update(any(), anyVararg())).thenReturn(1);
        int updateCount = sqlHelper.update("persons", "id", 2, ImmutableMap.of("age", 30));

        assertEquals(1, updateCount);
        verify(db, times(1)).update("UPDATE persons SET age =? WHERE id =?",  30, 2);
    }

    @Test
    public void delete()
    {
        when(db.update(any(), anyVararg())).thenReturn(1);
        int updateCount = sqlHelper.delete("persons", ImmutableMap.of("id", 2));

        assertEquals(1, updateCount);
        verify(db, times(1)).update("DELETE FROM persons WHERE id =?",  2);
    }

    @Test
    public void deleteIn()
    {
        when(db.update(any(), anyVararg())).thenReturn(2);
        int updateCount = sqlHelper.deleteIn("persons", "id", new Long[]{2L, 3L});

        assertEquals(2, updateCount);
        verify(db, times(1)).update("DELETE FROM persons WHERE id IN (?, ?)",  2L,  3L);
    }

}