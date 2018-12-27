package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.modules.core.services.CategoriesService;
import com.developmentontheedge.be5.modules.core.services.impl.model.MutableCategory;
import com.developmentontheedge.be5.modules.core.services.model.Category;
import com.developmentontheedge.be5.modules.core.util.Generators;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class CategoriesServiceImpl implements CategoriesService
{
    private final QueriesService queriesService;

    @Inject
    public CategoriesServiceImpl(QueriesService queriesService)
    {
        this.queriesService = queriesService;
    }

    @Override
    public List<Category> getCategoriesForest(String entityName, boolean hideEmpty)
    {
        List<MutableCategory> categories = queriesService
                .list("_categoriesService_", "getCategoriesForest",
                        Collections.singletonMap("entity", entityName), MutableCategory::fromResultSet);

        return getCategories(categories, hideEmpty);
    }

    @Override
    public List<Category> getRootCategory(String entityName)
    {
        return queriesService.list("_categoriesService_", "getRootCategory",
                        Collections.singletonMap("entity", entityName),
                rs -> new Category(
                        rs.getInt("ID"),
                        rs.getString("name"),
                        Collections.emptyList()));
    }

    @Override
    public List<Category> getCategoryNavigation(String entityName, long categoryID)
    {
        List<MutableCategory> categories = queriesService
                .list("_categoriesService_", "getCategoryNavigation",
                        ImmutableMap.of("categoryID", "" + categoryID, "entity", entityName),
                        MutableCategory::fromResultSet);

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
        Long value = queriesService.one("_categoriesService_", "hasAnyItem",
                Collections.singletonMap("categoryID", "" + category.id));
        return value != null && value > 0;
    }

}
