package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.server.services.model.Category;

import java.util.List;


public interface CategoriesService
{
    /**
     * Returns a list of root categories.
     */
    List<Category> getCategoriesForest(String entity, boolean hideEmpty);

    List<Category> getRootCategory(String entityName);

    List<Category> getCategoryNavigation(String entityName, long categoryID);
}