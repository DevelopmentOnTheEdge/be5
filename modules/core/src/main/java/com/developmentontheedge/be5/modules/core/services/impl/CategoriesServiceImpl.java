package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.model.Category;
import com.developmentontheedge.be5.modules.core.services.impl.model.MutableCategory;
import com.developmentontheedge.be5.util.Generators;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class CategoriesServiceImpl implements CategoriesService
{
    private final SqlService db;
    private final Meta meta;
    
    public CategoriesServiceImpl(SqlService db, OperationHelper operationHelper, Meta meta)
    {
        this.db = db;
        this.meta = meta;
    }

    @Override
    public List<Category> getCategoriesForest(String entityName, boolean hideEmpty)
    {
        List<MutableCategory> categories = db.selectList(
                meta.getQueryIgnoringRoles("_categoriesService_", "getCategoriesForest").getQuery(),
                MutableCategory::fromResultSet, entityName);
        //todo Be5QueryService

        return getCategories(categories, hideEmpty);
    }

    @Override
    public List<Category> getRootCategory(String entityName)
    {
        return db.selectList(meta.getQueryIgnoringRoles("_categoriesService_", "getRootCategory").getQuery(),
                rs -> new Category(rs.getInt("ID"), rs.getString("name"), Collections.emptyList()), entityName);
    }

    @Override
    public List<Category> getCategoryNavigation(long categoryID)
    {
        String sql = meta.getQueryIgnoringRoles("_categoriesService_", "getCategoryNavigation").getQuery();

        List<MutableCategory> categories = db.selectList(sql, MutableCategory::fromResultSet, categoryID, categoryID);
        return getCategories(categories, false);
    }

    private List<Category> getCategories(List<MutableCategory> categories, boolean hideEmpty)
    {
        List<MutableCategory> forest = Generators.forest(categories,
                c -> c.id,
                c -> c.parentId == null || c.parentId == 0,
                c -> c.parentId,
                (c, child) -> c.children.add(child));

        if (hideEmpty)
        {
            forest = removeLeafCategoriesWithNoItems(forest);
        }

        return MutableCategory.toCategories(forest);
    }

    private ImmutableList<MutableCategory> removeLeafCategoriesWithNoItems(List<MutableCategory> forest)
    {
        ImmutableList.Builder<MutableCategory> result = ImmutableList.builder();
        
        for (MutableCategory category : forest)
        {
            Optional<MutableCategory> r = removeLeafCategoriesWithNoItems(category);
            r.ifPresent(result::add);
        }
        
        return result.build();
    }
    
    private Optional<MutableCategory> removeLeafCategoriesWithNoItems(MutableCategory category)
    {
        ImmutableList.Builder<MutableCategory> childrenBuilder = ImmutableList.builder();
        
        for (MutableCategory child : category.children)
        {
            Optional<MutableCategory> c = removeLeafCategoriesWithNoItems(child);
            c.ifPresent(childrenBuilder::add);
        }
        
        ImmutableList<MutableCategory> children = childrenBuilder.build();
        
        if (children.isEmpty() && !hasAnyItem(category))
            return Optional.empty();
        
        return Optional.of(category.withChildren(children));
    }
    
    private boolean hasAnyItem(MutableCategory category)
    {
        return db.getLong(meta.getQueryIgnoringRoles("_categoriesService_", "hasAnyItem").getQuery(), category.id) > 0;
    }
    
}
