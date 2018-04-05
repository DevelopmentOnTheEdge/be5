package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.api.services.model.Category;

import java.util.List;


public interface CategoriesService
{
    /**
     * Returns a list of root categories.
     */
    List<Category> getCategoriesForest(String entity, boolean hideEmpty);

    List<Category> getRootCategory(String entityName);

    List<Category> getCategoryNavigation(long categoryID);
}
