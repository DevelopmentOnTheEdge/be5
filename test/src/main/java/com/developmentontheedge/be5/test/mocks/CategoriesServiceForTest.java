package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.server.services.CategoriesService;
import com.developmentontheedge.be5.server.services.model.Category;

import java.util.Collections;
import java.util.List;


public class CategoriesServiceForTest implements CategoriesService
{
    @Override
    public List<Category> getCategoriesForest(String entity, boolean hideEmpty)
    {
        return Collections.emptyList();
    }

    @Override
    public List<Category> getRootCategory(String entityName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<Category> getCategoryNavigation(String entityName, long categoryID)
    {
        return Collections.emptyList();
    }
}
