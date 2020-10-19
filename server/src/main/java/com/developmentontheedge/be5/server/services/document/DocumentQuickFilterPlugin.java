package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.QuickFilter;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentQuickFilterPlugin implements DocumentPlugin
{
    public static final String DOCUMENT_QUICK_FILTER_PLUGIN = "quickFilters";
    private final DocumentGenerator documentGenerator;

    @Inject
    public DocumentQuickFilterPlugin(DocumentGenerator documentGenerator)
    {
        this.documentGenerator = documentGenerator;
        documentGenerator.addDocumentPlugin(DOCUMENT_QUICK_FILTER_PLUGIN, this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        QuickFilter[] quickFilters = query.getQuickFilters();
        if (quickFilters.length > 0)
        {
            List<TablePresentation> filterData = Arrays.stream(quickFilters)
                    .map(qf -> documentGenerator.getTablePresentation(qf.getTargetQuery(), Collections.emptyMap()))
                    .collect(Collectors.toList());
            return new ResourceData(DOCUMENT_QUICK_FILTER_PLUGIN, filterData, null);
        }
        else
        {
            return null;
        }
    }
}
