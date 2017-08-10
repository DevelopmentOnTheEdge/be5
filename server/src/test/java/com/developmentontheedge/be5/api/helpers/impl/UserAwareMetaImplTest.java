package com.developmentontheedge.be5.api.helpers.impl;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserAwareMetaImplTest extends AbstractProjectTest
{
    @Test
    public void getLocalizedOperationTitle()
    {
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("default", "Delete"));
        assertEquals("Редактировать", userAwareMeta.getLocalizedOperationTitle("default", "Edit"));
        assertEquals("Добавить", userAwareMeta.getLocalizedOperationTitle("default", "Insert"));
        assertEquals("Фильтр", userAwareMeta.getLocalizedOperationTitle("default", "Filter"));
    }

    @Test
    public void getLocalizedOperationTitleUseDefault()
    {
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        assertEquals("Удалить", userAwareMeta.getLocalizedOperationTitle("anyTable", "Delete"));
    }

}