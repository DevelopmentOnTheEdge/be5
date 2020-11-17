package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.config.CoreUtils;
import com.developmentontheedge.be5.database.Transactional;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.model.MoreRowsPresentation;
import com.developmentontheedge.be5.server.model.RowsAsJsonPresentation;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.model.table.ColumnModel;
import com.developmentontheedge.be5.server.services.document.rows.MoreRowsBuilder;
import com.developmentontheedge.be5.server.services.document.rows.NamedCellsRowBuilder;
import com.developmentontheedge.be5.server.services.document.rows.RowsAsJsonBuilder;
import com.developmentontheedge.be5.server.services.document.rows.TableRowBuilder;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.util.FilterUtil;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.JsonUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.FrontendConstants.TABLE_JSON;
import static com.developmentontheedge.be5.FrontendConstants.TABLE_MORE_ACTION;
import static com.developmentontheedge.be5.query.QueryConstants.ALL_RECORDS;
import static com.developmentontheedge.be5.query.QueryConstants.LIMIT;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static java.util.Collections.singletonMap;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private final UserAwareMeta userAwareMeta;
    private final UserInfoProvider userInfoProvider;
    private final CoreUtils coreUtils;
    private final TableRowBuilder tableRowBuilder;
    private final QueryExecutorFactory queryServiceFactory;

    private final Map<String, DocumentPlugin> documentPlugins = new HashMap<>();

    @Inject
    public DocumentGeneratorImpl(UserAwareMeta userAwareMeta, UserInfoProvider userInfoProvider, CoreUtils coreUtils,
                                 TableRowBuilder tableRowBuilder, QueryExecutorFactory queryServiceFactory)
    {
        this.userAwareMeta = userAwareMeta;
        this.userInfoProvider = userInfoProvider;
        this.coreUtils = coreUtils;
        this.tableRowBuilder = tableRowBuilder;
        this.queryServiceFactory = queryServiceFactory;
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
    public JsonApiModel getDocument(Query query, Map<String, Object> params)
    {
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
                new ResourceData(TABLE_ACTION, data, singletonMap(SELF_LINK, getUrl(query, params))),
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
        String messageWhenEmpty = query.getMessageWhenEmpty();

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
                layout,
                messageWhenEmpty
        );
    }

    @Override
    public TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, List<QRec> rows)
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(query.getLayout());

        List<ColumnModel> columns = tableRowBuilder.collectColumns(query, rows);
        List finalRows = getRows(query, rows, layout);

        String title = "Query";
        String entityName = query.getEntity().getName();
        String queryName = query.getName();
        Long totalNumberOfRows = (long) rows.size();
        String messageWhenEmpty = query.getMessageWhenEmpty();

        return new TablePresentation(
                title,
                entityName, queryName,
                false,
                columns,
                finalRows,
                0, "asc",
                0, rows.size(),
                parameters,
                totalNumberOfRows,
                layout,
                messageWhenEmpty
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
        int limit = getLimit(query, layout, parameters);
        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        Map<String, Object> newParams = new HashMap<>(parameters);
        newParams.put(LIMIT, Math.min(limit, maxLimit) + "");
        return newParams;
    }

    int getLimit(Query query, Map<String, Object> layout, Map<String, Object> parameters)
    {
        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));
        int defaultLimit = Integer.parseInt(layout.getOrDefault("defaultPageLimit",
                coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        boolean isGuest = userInfoProvider.getCurrentRoles().contains(RoleType.ROLE_GUEST);
        Map<String, Object> userQuerySettings = isGuest
                ? Collections.emptyMap()
                : coreUtils.getQuerySettingForUser(query.getEntity().getName(), query.getName(),
                UserInfoHolder.getLoggedUser().getUserName());
        if (limit == Integer.MAX_VALUE)
        {
            return (int) userQuerySettings.getOrDefault("recordsPerPage", defaultLimit);
        }
        else
        {
            if (limit != defaultLimit && !isGuest || (userQuerySettings.get("recordsPerPage") != null
                    && limit != ((int) userQuerySettings.get("recordsPerPage"))))
            {
                coreUtils.setQuerySettingForUser(query.getEntity().getName(), query.getName(),
                        UserInfoHolder.getLoggedUser().getUserName(),
                        Collections.singletonMap("recordsPerPage", limit));
            }
            return limit;
        }
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
    JsonApiModel getNewTableRows(Query query, Map<String, Object> params)
    {
        MoreRowsPresentation data = getMoreRowsPresentation(query, params);

        return JsonApiModel.data(new ResourceData(TABLE_MORE_ACTION, data,
                singletonMap(SELF_LINK, getUrl(query, params))), null);
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

    @Override
    public JsonApiModel getTableRowsAsJson(String entityName, String queryName, Map<String, Object> parameters)
    {
        Query query = userAwareMeta.getQuery(entityName, queryName);
        return getTableRowsAsJson(query, parameters);
    }

    @LogBe5Event
    @Transactional
    JsonApiModel getTableRowsAsJson(Query query, Map<String, Object> params)
    {
        RowsAsJsonPresentation data = getRowsAsJsonPresentation(query, params);
        return JsonApiModel.data(new ResourceData(TABLE_JSON, data, null), null);
    }

    private RowsAsJsonPresentation getRowsAsJsonPresentation(Query query, Map<String, Object> parameters)
    {
        Map<String, Object> layout = JsonUtils.getMapFromJson(query.getLayout());

        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.MAX_VALUE + ""));
        int defaultLimit = Integer.parseInt((String) layout.getOrDefault("defaultPageLimit", Integer.MAX_VALUE + ""));
        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();
        Map<String, Object> paramsWithLimit = new HashMap<>(parameters);
        paramsWithLimit.put(LIMIT, Math.min(limit, Math.min(defaultLimit, maxLimit)));
        QueryExecutor queryExecutor = queryServiceFactory.get(query, paramsWithLimit);
        List<QRec> rows = queryExecutor.execute();
        return new RowsAsJsonPresentation(new RowsAsJsonBuilder(rows).build());
    }



    @Override
    public void addDocumentPlugin(String name, DocumentPlugin documentPlugin)
    {
        documentPlugins.put(name, documentPlugin);
    }

    private String getUrl(Query query, Map<String, Object> parameters)
    {
        return new HashUrl(TABLE_ACTION, query.getEntity().getName(), query.getName())
                .named(FilterUtil.getContextParams(parameters)).toString();
    }
}
