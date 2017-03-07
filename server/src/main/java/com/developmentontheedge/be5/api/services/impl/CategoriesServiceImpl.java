package com.developmentontheedge.be5.api.services.impl;

import java.util.List;
import java.util.Optional;

import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.util.Generators;
import com.google.common.collect.ImmutableList;

public class CategoriesServiceImpl implements CategoriesService
{
    
    private final SqlService db;
    
    public CategoriesServiceImpl(SqlService db)
    {
        this.db = db;
    }

    @Override
    public List<Category> getCategoriesForest(String entity, boolean hideEmpty)
    {
        List<MutableCategory> categories = db.from("categories").selectWith("entity", entity, MutableCategory::fromResultSet);
        List<MutableCategory> forest = Generators.forest(categories,
            c -> c.id,
            c -> c.parentId == 0,
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
            if (r.isPresent())
                result.add(r.get());
        }
        
        return result.build();
    }
    
    private Optional<MutableCategory> removeLeafCategoriesWithNoItems(MutableCategory category)
    {
        ImmutableList.Builder<MutableCategory> childrenBuilder = ImmutableList.builder();
        
        for (MutableCategory child : category.children)
        {
            Optional<MutableCategory> c = removeLeafCategoriesWithNoItems(child);
            if (c.isPresent())
                childrenBuilder.add(c.get());
        }
        
        ImmutableList<MutableCategory> children = childrenBuilder.build();
        
        if (children.isEmpty() && !hasAnyItem(category))
            return Optional.empty();
        
        return Optional.of(category.withChildren(children));
    }
    
    private boolean hasAnyItem(MutableCategory category)
    {
        return db.in("classifications").existsWith("categoryID", category.id);
    }
    
}
