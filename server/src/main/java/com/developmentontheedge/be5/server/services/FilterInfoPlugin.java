package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.util.FilterUtil;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.ColumnDef;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.QueryMetaHelper;
import com.developmentontheedge.be5.query.services.QueriesService;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.sql.model.AstBeParameterTag;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;


public class FilterInfoPlugin implements DocumentPlugin
{
    private final QueriesService queries;
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final QueryMetaHelper queryMetaHelper;
    protected static final String DOCUMENT_FILTER_INFO_PLUGIN = "filterInfo";

    @Inject
    public FilterInfoPlugin(QueriesService queries, Meta meta, UserAwareMeta userAwareMeta,
                            DocumentGenerator documentGenerator, QueryMetaHelper queryMetaHelper)
    {
        this.queries = queries;
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.queryMetaHelper = queryMetaHelper;
        documentGenerator.addDocumentPlugin(DOCUMENT_FILTER_INFO_PLUGIN, this);
    }

    @Override
    public ResourceData addData(Query query, Map<String, Object> parameters)
    {
        FilterInfo filterInfo = new FilterInfo(getOperationParamsInfo(query, parameters));
        return new ResourceData(DOCUMENT_FILTER_INFO_PLUGIN, filterInfo, null);
    }

    protected List<FilterItem> getOperationParamsInfo(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = FilterUtil.getOperationParamsWithoutFilter(parameters);
        List<FilterItem> result = new ArrayList<>();

        if (query.getType() == QueryType.D1 || query.getType() == QueryType.D1_UNKNOWN)
        {
            AstStart ast = SqlQuery.parse(query.getFinalQuery());
            Map<String, AstBeParameterTag> usedParams = ast.tree()
                    .select(AstBeParameterTag.class)
                    .collect(Collectors.toMap(AstBeParameterTag::getName, identity(),
                            (param1, param2) -> param1));

            if (params.containsKey("entity") && params.containsKey("entityID"))
            {
                String entity = (String) params.remove("entity");
                String entityID = (String) params.remove("entityID");
                ColumnDef column = meta.getColumn(entity, meta.getEntity(entity).getPrimaryKey());

                String[][] tags = queries.getTagsFromSelectionView(column.getTableFrom(),
                        Collections.singletonMap(column.getName(), entityID));
                String entityTitle = userAwareMeta.getLocalizedEntityTitle(column.getTableFrom());
                if (tags.length > 0) result.add(new FilterItem(entityTitle, tags[0][1]));
            }

            String mainTableDefName = QueryMetaHelper.getMainTableDefName(ast);
            params.forEach((k, v) -> {
                if (mainTableDefName != null && meta.hasEntity(mainTableDefName))
                {
                    ColumnDef column = meta.getColumn(mainTableDefName, k);
                    if (column != null)
                    {
                        result.add(getValueTitle(column, mainTableDefName, k, v));
                        return;
                    }
                }

                if (query.getType() == QueryType.D1 || query.getType() == QueryType.D1_UNKNOWN)
                {
                    if (usedParams.containsKey(k))
                    {
                        ColumnDef column2 = queryMetaHelper.getColumnDef(ast, usedParams.get(k), mainTableDefName);
                        if (column2 != null)
                        {
                            result.add(getValueTitle(column2, mainTableDefName, k, v));
                            return;
                        }
                    }
                }

                String valueTitle = mainTableDefName != null ?
                        userAwareMeta.getColumnTitle(mainTableDefName, query.getName(), v + "") : v + "";
                result.add(new FilterItem(mainTableDefName != null ?
                        userAwareMeta.getColumnTitle(mainTableDefName, k) : k, valueTitle));
            });
        }
        return result;
    }

    protected FilterItem getValueTitle(ColumnDef column, String mainEntityName, String k, Object v)
    {
        String columnTitle = userAwareMeta.getColumnTitle(column.getTableFrom(), k);
        if (meta.getEntity(column.getTableFrom()).getPrimaryKey().equals(column.getName()))
        {
            String[][] tags = queries.getTagsFromSelectionView(column.getTableFrom(),
                    Collections.singletonMap(meta.getEntity(column.getTableFrom()).getPrimaryKey(), v));
            String idColumnTitle = mainEntityName.equalsIgnoreCase(column.getTableFrom()) ? null : columnTitle;
            if (tags.length > 0) return new FilterItem(idColumnTitle, tags[0][1]);
        }
        if (column.getTableTo() != null)
        {
            String[][] tags = queries.getTagsFromSelectionView(column.getTableTo(),
                    Collections.singletonMap(meta.getEntity(column.getTableTo()).getPrimaryKey(), v));
            if (tags.length > 0) return new FilterItem(columnTitle, tags[0][1]);
        }
        return new FilterItem(columnTitle, v + "");
    }

    public static class FilterItem
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

    public static class FilterInfo
    {
        private List<FilterItem> operationParamsInfo;

        public FilterInfo(List<FilterItem> operationParamsInfo)
        {
            this.operationParamsInfo = operationParamsInfo;
        }

        public List<FilterItem> getOperationParamsInfo()
        {
            return operationParamsInfo;
        }
    }

}
