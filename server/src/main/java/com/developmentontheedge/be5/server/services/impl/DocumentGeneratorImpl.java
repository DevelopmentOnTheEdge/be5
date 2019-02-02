package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.database.Transactional;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.MoreRowsPresentation;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.model.table.ColumnModel;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.server.services.impl.rows.MoreRowsBuilder;
import com.developmentontheedge.be5.server.services.impl.rows.NamedCellsRowBuilder;
import com.developmentontheedge.be5.server.services.impl.rows.TableRowBuilder;
import com.developmentontheedge.be5.util.FilterUtil;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PARAM;
import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM;
import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.FrontendConstants.TABLE_MORE_ACTION;
import static com.developmentontheedge.be5.query.QueryConstants.ALL_RECORDS;
import static com.developmentontheedge.be5.query.QueryConstants.CLEAN_NAV;
import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.query.QueryConstants.OFFSET;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.QueryConstants.ORDER_DIR;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static java.util.Collections.singletonMap;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private static final String QUERY_POSITIONS = "QUERY_POSITIONS";
    private static final String QUERY_FILTER = "QUERY_FILTER";
    private static final List<String> positionsParamNames = Arrays.asList(ORDER_COLUMN, ORDER_DIR, OFFSET, LIMIT);

    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final TableRowBuilder tableRowBuilder;
    private final QueryExecutorFactory queryServiceFactory;
    private final Provider<Session> session;

    private final Map<String, DocumentPlugin> documentPlugins = new HashMap<>();

    @Inject
    public DocumentGeneratorImpl(UserAwareMeta userAwareMeta, CoreUtils coreUtils, TableRowBuilder tableRowBuilder,
                                 QueryExecutorFactory queryServiceFactory, Provider<Session> session)
    {
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.tableRowBuilder = tableRowBuilder;
        this.queryServiceFactory = queryServiceFactory;
        this.session = session;
    }

    @Override
    public JsonApiModel getDocument(String entityName, String queryName, Map<String, Object> parameters)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        return getDocument(query, parameters);
    }

    @Override
    @LogBe5Event
    @Transactional
    public JsonApiModel getDocument(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = processQueryParams(query, parameters);
        TablePresentation data;
        List<ResourceData> included = new ArrayList<>();
        try
        {
            data = getTablePresentation(query, params);
            documentPlugins.forEach((k, v) -> {
                ResourceData resourceData = v.addData(query, params);
                if (resourceData != null) included.add(resourceData);
            });
        }
        catch (RuntimeException e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }

        return JsonApiModel.data(
                new ResourceData(TABLE_ACTION, data, singletonMap(SELF_LINK, getUrl(query, parameters))),
                included.toArray(new ResourceData[0]),
                null
        );
    }

    @Override
    public TablePresentation getTablePresentation(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(query.getLayout());
        Map<String, Object> paramsWithLimit = updateLimit(query, layout, parameters);
        QueryExecutor queryExecutor = queryServiceFactory.get(query, paramsWithLimit);
        List<QRec> rows = queryExecutor.execute();

        List<ColumnModel> columns = tableRowBuilder.collectColumns(query, rows);
        List finalRows = getRows(query, rows, layout);

        String title = getTitle(query, layout);
        String entityName = query.getEntity().getName();
        String queryName = query.getName();
        Long totalNumberOfRows = getCount(queryExecutor, rows.size());

        return new TablePresentation(
                title,
                entityName, queryName,
                queryExecutor.isSelectable(),
                columns,
                finalRows,
                queryExecutor.getOrderColumn(), queryExecutor.getOrderDir(),
                queryExecutor.getOffset(), queryExecutor.getLimit(),
                parameters,
                totalNumberOfRows,
                layout
        );
    }

    private long getCount(QueryExecutor queryExecutor, long rowsCount)
    {
        if (queryExecutor.getOffset() + rowsCount < queryExecutor.getLimit())
        {
            return rowsCount;
        }
        else
        {
            return queryExecutor.count();
        }
    }

    private Map<String, Object> updateLimit(Query query, Map<String, Object> layout, Map<String, Object> parameters)
    {
        HashMap<String, Object> newParams = new HashMap<>(parameters);
        int limit = Integer.parseInt((String) newParams.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            limit = Integer.parseInt(layout.getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }
        newParams.put(LIMIT, Math.min(limit, maxLimit) + "");
        return newParams;
    }

    private List getRows(Query query, List<QRec> rows, Map<String, Object> layout)
    {
        String mode = (String) layout.getOrDefault("mode", "");
        if (mode.equals("named"))
        {
            return new NamedCellsRowBuilder(rows).build();
        }
        else
        {
            return tableRowBuilder.collectRows(query, rows);
        }
    }

    private String getTitle(Query query, Map<String, Object> layout)
    {
        if (layout.get("title") != null)
        {
            return userAwareMeta.getLocalizedQueryTitle(query.getEntity().getName(), (String) layout.get("title"));
        }
        else if (ALL_RECORDS.equals(query.getName()))
        {
            return userAwareMeta.getLocalizedEntityTitle(query.getEntity());
        }
        else
        {
            String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(query.getEntity());
            String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(
                    query.getEntity().getName(), query.getName());
            return localizedEntityTitle + ": " + localizedQueryTitle;
        }
    }

    @Override
    public JsonApiModel getNewTableRows(String entityName, String queryName, Map<String, Object> parameters)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        return getNewTableRows(query, parameters);
    }

    @LogBe5Event
    @Transactional
    JsonApiModel getNewTableRows(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = processQueryParams(query, parameters);
        MoreRowsPresentation data = getMoreRowsPresentation(query, params);

        return JsonApiModel.data(new ResourceData(TABLE_MORE_ACTION, data,
                singletonMap(SELF_LINK, getUrl(query, parameters))), null);
    }

    private MoreRowsPresentation getMoreRowsPresentation(Query query, Map<String, Object> params)
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(query.getLayout());
        Map<String, Object> paramsWithLimit = updateLimit(query, layout, params);
        QueryExecutor queryExecutor = queryServiceFactory.get(query, paramsWithLimit);
        List<QRec> rows = queryExecutor.execute();
        long count = getCount(queryExecutor, rows.size());
        return new MoreRowsPresentation(
                count,
                count,
                new MoreRowsBuilder(tableRowBuilder.collectRows(query, rows)).build()
        );
    }

    private Map<String, Object> processQueryParams(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> params = new LinkedHashMap<>(parameters);
        if (parameters.containsKey(CLEAN_NAV))
        {
            params.remove(CLEAN_NAV);
            String queryKey = getQueryKey(query, parameters);
            getUserQueriesPositions().remove(queryKey);
            getUserQueriesFilterParams().remove(queryKey);
            return params;
        }
        else
        {
            addSavedPositionIfNotExist(query, params);
            addSavedFilterParamsIfNotExist(query, params);
            return params;
        }
    }

    @Override
    public void addDocumentPlugin(String name, DocumentPlugin documentPlugin)
    {
        documentPlugins.put(name, documentPlugin);
    }

    private String getUrl(Query query, Map<String, Object> parameters)
    {
        return new HashUrl(TABLE_ACTION, query.getEntity().getName(), query.getName())
                .named(FilterUtil.getOperationParamsWithoutFilter(parameters)).toString();
    }

    private Map<String, Map<String, Object>> getUserQueriesPositions()
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> filterParams =
                (Map<String, Map<String, Object>>) session.get().get(QUERY_POSITIONS);
        if (filterParams == null)
        {
            filterParams = new HashMap<>();
            session.get().set(QUERY_POSITIONS, filterParams);
        }
        return filterParams;
    }

    private Map<String, Map<String, Object>> getUserQueriesFilterParams()
    {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> filterParams =
                (Map<String, Map<String, Object>>) session.get().get(QUERY_FILTER);
        if (filterParams == null)
        {
            filterParams = new HashMap<>();
            session.get().set(QUERY_FILTER, filterParams);
        }
        return filterParams;
    }

    @Override
    public void clearSavedPosition(Query query, Map<String, Object> parameters)
    {
        getUserQueriesPositions().remove(getQueryKey(query, parameters));
    }

    private void addSavedPositionIfNotExist(Query query, Map<String, Object> parameters)
    {
        Map<String, Map<String, Object>> positions = getUserQueriesPositions();
        String queryKey = getQueryKey(query, parameters);
        if (parameters.containsKey(ORDER_COLUMN) || parameters.containsKey(ORDER_DIR) ||
                parameters.containsKey(OFFSET) || parameters.containsKey(LIMIT))
        {
            Map<String, Object> newPos = new HashMap<>();
            positionsParamNames.forEach(name -> {
                if (parameters.containsKey(name)) newPos.put(name, parameters.get(name));
            });
            positions.put(queryKey, newPos);
        }
        else
        {
            Map<String, Object> savedPosition = positions.get(queryKey);
            if (savedPosition != null) parameters.putAll(savedPosition);
        }
    }

    private void addSavedFilterParamsIfNotExist(Query query, Map<String, Object> parameters)
    {
        Map<String, Map<String, Object>> filterParams = getUserQueriesFilterParams();
        String queryKey = getQueryKey(query, parameters);
        if (parameters.containsKey(SEARCH_PARAM))
        {
            filterParams.put(queryKey, FilterUtil.getFilterParams(parameters));
        }
        else
        {
            if (filterParams.containsKey(queryKey))
            {
                String searchPresetParam = FilterUtil.getSearchPresetParam(parameters);
                if (searchPresetParam != null) parameters.put(SEARCH_PRESETS_PARAM, searchPresetParam);
                parameters.putAll(filterParams.get(queryKey));
                parameters.put(SEARCH_PARAM, "true");
            }
        }
    }

    private String getQueryKey(Query query, Map<String, Object> parameters)
    {
        return new HashUrl(query.getEntity().getName(), query.getName()).named(
                FilterUtil.getOperationParamsWithoutFilter(parameters)).toString();
    }
}
