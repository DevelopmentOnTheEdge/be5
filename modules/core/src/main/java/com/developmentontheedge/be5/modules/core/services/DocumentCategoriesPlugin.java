package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.modules.core.services.model.Category;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.DocumentGenerator;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.base.FrontendConstants.CATEGORY_ID_PARAM;


public class DocumentCategoriesPlugin implements DocumentPlugin
{
    private final CategoriesService categoriesService;
    private final String documentPluginName = "documentCategories";

    @Inject
    public DocumentCategoriesPlugin(CategoriesService categoriesService, DocumentGenerator documentGenerator, Meta meta)
    {
        this.categoriesService = categoriesService;
        if(meta.hasFeature(documentPluginName))
        {
            documentGenerator.addDocumentPlugin(this);
        }
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        return new ResourceData(
                documentPluginName,
                getCategoryNavigation(query.getEntity().getName(), (String) parameters.get(CATEGORY_ID_PARAM)),
                null
        );
    }

    private List<Category> getCategoryNavigation(String entityName, String categoryID)
    {
        if (categoryID != null)
        {
            return categoriesService.getCategoryNavigation(entityName, Long.parseLong(categoryID));
        }
        else
        {
            return categoriesService.getRootCategory(entityName);
        }
    }

}
