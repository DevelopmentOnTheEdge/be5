package com.developmentontheedge.be5.modules.core.services.impl;

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
    
    public CategoriesServiceImpl(SqlService db)
    {
        this.db = db;
    }

    @Override
    public List<Category> getCategoriesForest(String entityName, boolean hideEmpty)
    {
        List<MutableCategory> categories = db.selectList("SELECT * FROM categories WHERE entity = ?",
                MutableCategory::fromResultSet, entityName);

        return getCategories(categories, hideEmpty);
    }

    @Override
    public List<Category> getRootCategory(String entityName)
    {
        return db.selectList("SELECT ID, name FROM categories WHERE entity = ? AND (parentID IS NULL OR parentID = 0)",
                rs -> new Category(rs.getInt("ID"), rs.getString("name"), Collections.emptyList()), entityName);
    }

    @Override
    public List<Category> getCategoryNavigation(long categoryID)
    {
        String sql = "SELECT DISTINCT c1.ID, c1.name, c1.parentId from categories c1\n" +
            "LEFT JOIN categories c2 on c2.parentID = c1.ID\n" +
            "LEFT JOIN categories c3 on c3.parentID = c2.ID\n" +
            "LEFT JOIN categories c4 on c4.parentID = c3.ID\n" +
            "LEFT JOIN categories c5 on c5.parentID = c4.ID\n" +
            "LEFT JOIN categories c6 on c6.parentID = c5.ID\n" +
            "LEFT JOIN categories c7 on c7.parentID = c6.ID\n" +
            "LEFT JOIN categories c8 on c8.parentID = c7.ID\n" +
            "WHERE ? IN ( c1.ID, c2.ID, c3.ID, c4.ID, c5.ID, c6.ID, c7.ID, c8.ID ) OR (c1.parentID = ?)";

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
        return db.getLong("SELECT COUNT(*) FROM classifications WHERE categoryID = ?", category.id) > 0;
    }
    
}
