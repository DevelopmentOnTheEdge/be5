package com.developmentontheedge.be5.base.services;

import com.developmentontheedge.be5.base.BaseTest;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class UserAwareMetaImplTest extends BaseTest
{
    @Inject private UserAwareMeta userAwareMeta;

    @Test
    public void getLocalizedOperationTitle()
    {
        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("default", "Delete"));
        assertEquals("Редактировать", userAwareMeta.getLocalizedOperationTitle("default", "Edit"));
        assertEquals("Добавить", userAwareMeta.getLocalizedOperationTitle("default", "Insert"));
        assertEquals("Фильтр", userAwareMeta.getLocalizedOperationTitle("default", "Filter"));
    }

    @Test
    public void getLocalizedOperationTitleUseDefault()
    {
        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("anyTable", "Delete"));
    }

}