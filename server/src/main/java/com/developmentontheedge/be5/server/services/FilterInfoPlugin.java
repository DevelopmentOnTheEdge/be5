package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.utils.QueryUtils;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
            return new ResourceData("filterInfo", map, null);
        }

        return null;
    }

    private List<FilterItem> getOperationParamsInfo(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = FilterUtil.getOperationParamsWithoutFilter(parameters);
        List<FilterItem> result = new ArrayList<>();
        String mainEntityName = query.getEntity().getName();
        params.forEach((k, v) -> {
            ColumnDef column = meta.getColumn(mainEntityName, k);
            if (column != null)
            {
                result.add(getValueTitle(column, mainEntityName, k, v));
                return;
            }

            AstStart ast = SqlQuery.parse(meta.getQueryCode(query));
            Optional<AstBeParameterTag> usedParam = ast.tree()
                    .select(AstBeParameterTag.class)
                    .filter(x -> x.getName().equals(k))
                    .findFirst();

            if (usedParam.isPresent())
            {
                ColumnDef column2 = QueryUtils.getColumnDef(ast, usedParam.get(), mainEntityName, meta);
                result.add(getValueTitle(column2, mainEntityName, k, v));
                return;
            }

            String valueTitle = userAwareMeta.getColumnTitle(mainEntityName, query.getName(), v + "");
            result.add(new FilterItem(k, valueTitle));
        });
        return result;
    }

    private FilterItem getValueTitle(ColumnDef column, String mainEntityName, String k, Object v)
    {
        String columnTitle = userAwareMeta.getColumnTitle(column.getTableFrom(), k);
        if (meta.getEntity(column.getTableFrom()).getPrimaryKey().equals(column.getName()))
        {
            String[][] tags = queries.getTagsFromSelectionView(column.getTableFrom(),
                    Collections.singletonMap(meta.getEntity(column.getTableFrom()).getPrimaryKey(), v));
            String idColumnTitle = mainEntityName.equalsIgnoreCase(column.getTableFrom()) ? null : columnTitle;
            if (tags.length > 0) return new FilterItem(idColumnTitle, tags[0][1]);
        }
        if (column.getTableTo() != null && meta.getEntity(column.getTableTo()) != null)
        {
            String[][] tags = queries.getTagsFromSelectionView(column.getTableTo(),
                    Collections.singletonMap(meta.getEntity(column.getTableTo()).getPrimaryKey(), v));
            if (tags.length > 0) return new FilterItem(columnTitle, tags[0][1]);
        }
        return new FilterItem(columnTitle, v + "");
    }

    public class FilterItem
    {
        private String key;
        private String value;

        public FilterItem(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }

}
