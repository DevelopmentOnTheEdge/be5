package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DocumentQuickFilterPlugin implements DocumentPlugin
{
    public static final String DOCUMENT_QUICK_FILTER_PLUGIN = "quickFilters";
    private final QueriesService queries;

    @Inject
    public DocumentQuickFilterPlugin(DocumentGenerator documentGenerator, QueriesService queries)
    {
        this.queries = queries;
        documentGenerator.addDocumentPlugin(DOCUMENT_QUICK_FILTER_PLUGIN, this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        QuickFilter[] quickFilters = query.getQuickFilters();
        if (quickFilters.length > 0)
        {
            QuickFilterInfo filterData = new QuickFilterInfo(new ArrayList<>(quickFilters.length));
            for (QuickFilter quickFilter : quickFilters)
            {
                String[][] tags = queries.getTagsFromCustomSelectionView(quickFilter.getTargetQuery(), Collections.emptyMap());
                tags = queries.localizeTags(query.getEntity().getName(), quickFilter.getTargetQuery().getName(), tags);
                filterData.add(new QuickFilterItem(quickFilter.getName(), quickFilter.getQueryParam(), tags));
            }
            return new ResourceData(DOCUMENT_QUICK_FILTER_PLUGIN, filterData, null);
        }
        else
        {
            return null;
        }
    }

    public static class QuickFilterItem
    {
        private final String title;
        private final String param;
        private final String[][] tags;

        public QuickFilterItem(String title, String param, String[][] tags)
        {
            this.title = title;
            this.param = param;
            this.tags = tags;
        }

        public String getTitle()
        {
            return title;
        }

        public String getParam()
        {
            return param;
        }

        public String[][] getTags()
        {
            return tags;
        }
    }

    public static class QuickFilterInfo
    {
        private final List<QuickFilterItem> quickFilterInfo;

        public QuickFilterInfo(List<QuickFilterItem> quickFilterInfo)
        {
            this.quickFilterInfo = quickFilterInfo;
        }

        public List<QuickFilterItem> getQuickFilterInfo()
        {
            return quickFilterInfo;
        }

        boolean add(QuickFilterItem item)
        {
            return quickFilterInfo.add(item);
        }
    }
}
