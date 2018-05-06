package com.developmentontheedge.be5.api.helpers.impl;

import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import javax.inject.Inject;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserAwareMetaImplTest extends Be5ProjectTest
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