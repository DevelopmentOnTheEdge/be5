package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDbMockTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class CategoriesControllerTest extends CoreBe5ProjectDbMockTest
{
    @Inject
    private CategoriesController component;

    @Test
    public void generate()
    {
        assertEquals(new ArrayList<>(),
                component.generate(getMockRequest("/api/categories/forest"), "forest"));
    }

    @Test
    public void sendUnknownActionError()
    {
        assertEquals(null, component.generate(getMockRequest("/api/categories/"), ""));
    }
}