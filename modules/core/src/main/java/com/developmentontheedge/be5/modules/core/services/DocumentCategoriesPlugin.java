package com.developmentontheedge.be5.modules.core.services;

import com.developmentontheedge.be5.lifecycle.Start;
import com.developmentontheedge.be5.meta.ProjectProvider;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.modules.core.services.model.Category;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.document.DocumentGenerator;
import com.developmentontheedge.be5.server.services.document.DocumentPlugin;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.CATEGORY_ID_PARAM;


public class DocumentCategoriesPlugin implements DocumentPlugin
{
    private final CategoriesService categoriesService;
    private final DocumentGenerator documentGenerator;
    private final ProjectProvider projectProvider;

    @Inject
    public DocumentCategoriesPlugin(CategoriesService categoriesService, DocumentGenerator documentGenerator,
                                    ProjectProvider projectProvider)
    {
        this.categoriesService = categoriesService;
        this.documentGenerator = documentGenerator;
        this.projectProvider = projectProvider;
    }

    @Start(order = 30)
    public void start() throws Exception
    {
        if (projectProvider.get().hasFeature(Features.DOCUMENT_CATEGORIES))
        {
            documentGenerator.addDocumentPlugin(Features.DOCUMENT_CATEGORIES, this);
        }
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        List<Category> categories =
                getCategoryNavigation(query.getEntity().getName(), (String) parameters.get(CATEGORY_ID_PARAM));
        if (categories.size() > 0)
        {
            return new ResourceData(Features.DOCUMENT_CATEGORIES, categories, null);
        }
        return null;
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
