package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class FilterInfoPlugin implements DocumentPlugin
{
    private final QueriesService queries;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;

    @Inject
    public FilterInfoPlugin(QueriesService queries, Meta meta, UserAwareMeta userAwareMeta)
    {
        this.queries = queries;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("operationParamsInfo", getOperationParamsInfo(query, parameters));
        if (map.size() > 0)
        {
            return new ResourceData("documentOperations", map, null);
        }

        return null;
    }

    private Map<String, String> getOperationParamsInfo(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = FilterUtil.getOperationParamsWithoutFilter(parameters);
        Map<String, String> result = new HashMap<>();
        params.forEach((k,v) -> {
            String columnTitle = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), k);
            String valueTitle = v + "";
            ColumnDef column = meta.getColumn(query.getEntity().getName(), k);
            if (column != null && column.getTableTo() != null)
            {
                String[][] tags = queries.getTagsFromSelectionView(column.getTableTo(),
                        Collections.singletonMap(meta.getEntity(column.getTableTo()).getPrimaryKey(), v));
                if (tags.length > 0) valueTitle = tags[0][1];
            }
            else
            {
                valueTitle = userAwareMeta.getColumnTitle(query.getEntity().getName(), query.getName(), v + "");
            }
            result.put(columnTitle, valueTitle);
        });
        return result;
    }

}
