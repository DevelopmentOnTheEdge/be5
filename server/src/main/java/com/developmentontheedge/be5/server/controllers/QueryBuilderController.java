package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.database.DataSourceService;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelElementSupport;
import com.developmentontheedge.be5.query.impl.QuerySqlGenerator;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.database.adapters.ListWrapperHandler;
import com.developmentontheedge.be5.database.adapters.QRecParser;
import com.developmentontheedge.be5.database.util.DynamicPropertyMeta;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.services.document.DocumentGenerator;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.sql.model.AstDelete;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstUpdate;
import com.developmentontheedge.sql.model.DbSpecificFunction;
import com.developmentontheedge.sql.model.DefaultParserContext;
import com.developmentontheedge.sql.model.SqlQuery;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.server.SessionConstants.QUERY_BUILDER_HISTORY;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

@Singleton
public class QueryBuilderController extends JsonApiModelController
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final DbService db;
    private final DocumentGenerator documentGenerator;
    private final Meta meta;
    private final ErrorModelHelper errorModelHelper;
    private final UserInfoProvider userInfoProvider;
    private final QuerySqlGenerator querySqlGenerator;
    private final DataSourceService dataSourceService;

    @Inject
    public QueryBuilderController(DbService db, DocumentGenerator documentGenerator, Meta meta,
                                  ErrorModelHelper errorModelHelper, UserInfoProvider userInfoProvider,
                                  QuerySqlGenerator querySqlGenerator, DataSourceService dataSourceService)
    {
        this.db = db;
        this.documentGenerator = documentGenerator;
        this.meta = meta;
        this.errorModelHelper = errorModelHelper;
        this.userInfoProvider = userInfoProvider;
        this.querySqlGenerator = querySqlGenerator;
        this.dataSourceService = dataSourceService;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String requestSubUrl)
    {
        if ("editor".equals(requestSubUrl))
        {
            return getEditorData();
        }
        return executeQuery(req);
    }

    JsonApiModel getEditorData()
    {
        List<String> functions = DefaultParserContext.getInstance().getFunctionsMap()
                .entrySet().stream().filter(x -> (!(x.getValue() instanceof DbSpecificFunction)
                        || ((DbSpecificFunction) x.getValue()).isApplicable(dataSourceService.getDbms())))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        return data(new ResourceData("editor", ImmutableMap.of(
                "functions", functions,
                "tableNames", meta.getEntities().stream()
                        .map(BeModelElementSupport::getName)
                        .filter(x -> !(x.startsWith("_") && x.endsWith("_")))
                        .collect(Collectors.toList())
        ), null));
    }

    private JsonApiModel executeQuery(Request req)
    {
        List<ResourceData> includedData = new ArrayList<>();
        List<ErrorModel> errorModelList = new ArrayList<>();

        if (userInfoProvider.isSystemDeveloper())
        {
            String sql = req.get("sql");
            boolean execute = sql != null;
            List<String> history = getHistory(req);

            if (sql == null)
            {
                sql = history.get(history.size() - 1).trim();
            }
            else
            {
                sql = sql.trim();
                if (!history.get(history.size() - 1).equals(sql))
                {
                    history.add(sql);
                    req.getSession().set(QUERY_BUILDER_HISTORY, history);
                }
            }

            Data data;
            try
            {
                if (req.getBoolean("updateWithoutBeSql", false))
                {
                    data = new Data("", "", history);
                    executeRaw(includedData, sql);
                }
                else
                {
                    SqlType type = getSqlType(sql);
                    if (type == SqlType.SELECT)
                    {
                        data = new Data(sql, select(includedData, errorModelList, sql, req), history);
                    }
                    else
                    {
                        data = new Data("", db.format(sql), history);
                        if (execute)
                        {
                            switch (type)
                            {
                                case INSERT:
                                    insert(includedData, sql);
                                    break;
                                case UPDATE:
                                    update(includedData, sql);
                                    break;
                                case DELETE:
                                    update(includedData, sql);
                                    break;
                                default:
                                    return null;
                            }
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                //log.log(Level.SEVERE, "error on getFinalSql", e);
                data = new Data(sql, "", history);
                errorModelList.add(errorModelHelper.getErrorModel(Be5Exception.internal(e)));
            }

            Map<String, Object> parameters = ParseRequestUtils.getContextParams(req.get(RestApiConstants.CONTEXT_PARAMS));
            ResourceData resourceData = new ResourceData(
                    "queryBuilder",
                    data,
                    singletonMap(SELF_LINK, new HashUrl("queryBuilder").named(parameters).toString())
            );

            return data(
                    resourceData,
                    errorModelList.toArray(new ErrorModel[0]),
                    includedData.toArray(new ResourceData[0])
            );
        }
        else
        {
            return error(errorModelHelper.getErrorModel(
                    Be5Exception.accessDenied("Role " + RoleType.ROLE_SYSTEM_DEVELOPER + " required."),
                    singletonMap(SELF_LINK, "queryBuilder"))
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getHistory(Request req)
    {
        if (req.getSession().get(QUERY_BUILDER_HISTORY) != null)
        {
            return (List<String>) req.getSession().get(QUERY_BUILDER_HISTORY);
        }
        else
        {
            List<String> newHistory = new ArrayList<>();
            newHistory.add("select * from users");
            return newHistory;
        }
    }

    private void insert(List<ResourceData> includedData, String sql)
    {
        Object id = db.insert(sql);
        addText(includedData, "Insert was successful", "New primaryKey: " + id);
    }

    private void update(List<ResourceData> includedData, String sql)
    {
        Object id = db.update(sql);
        addText(includedData, "Update was successful", id + " row(s) affected");
    }

    void executeRaw(List<ResourceData> includedData, String sql)
    {
        if (sql.startsWith("update") || sql.startsWith("UPDATE")
                || sql.startsWith("insert") || sql.startsWith("INSERT")
                || sql.startsWith("delete") || sql.startsWith("DELETE"))
        {
            Object id = db.updateRaw(sql);
            addText(includedData, "Update was successful", id + " row(s) affected");
        }
        else
        {
            String finalSql = processRawSql(sql);
            List<List<QRec>> lists = db.executeRaw(finalSql, new ListWrapperHandler<>(new QRecParser()));
            if (lists.size() > 0)
            {
                List<QRec> qRecList = lists.get(0);
                if (qRecList.size() > 0)
                {
                    qRecList.get(0).forEach(dp -> DynamicPropertyMeta.add(dp, singletonMap("nosort", emptyMap())));
                }
                TablePresentation tablePresentation = documentGenerator.
                        getTablePresentation(meta.createQueryFromSql(finalSql), emptyMap(), qRecList);
                includedData.add(new ResourceData(
                        "result",
                        FrontendConstants.TABLE_ACTION,
                        tablePresentation,
                        null
                ));
            }
            else
            {
                addText(includedData, "Execute no return data", "");
            }
        }
    }

    private String processRawSql(String sql)
    {
        if (!(sql.contains("limit") || sql.contains("LIMIT")) &&
                (sql.startsWith("SELECT") || sql.startsWith("select")))
        {
            return sql + "\nLIMIT 100";
        }
        return sql;
    }

    private void addText(List<ResourceData> includedData, String title, String text)
    {
        includedData.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(title, text),
                null
        ));
    }

    private String select(List<ResourceData> includedData, List<ErrorModel> errorModelList, String sql, Request req)
    {
        Map<String, Object> parameters = ParseRequestUtils.getContextParams(req.get(RestApiConstants.CONTEXT_PARAMS));
        Query query = meta.createQueryFromSql(sql);
        String finalSql = getFinalSql(errorModelList, query, parameters);

        try
        {
            JsonApiModel document = documentGenerator.getDocument(query, parameters);

            document.getData().setId("result");
            includedData.add(document.getData());
            includedData.addAll(Arrays.asList(document.getIncluded()));
        }
        catch (Be5Exception e)
        {
            log.log(Level.SEVERE, "Error in queryBuilder", e);
            errorModelList.add(errorModelHelper.getErrorModel(e));
        }
        return finalSql;
    }

    private String getFinalSql(List<ErrorModel> errorModelList, Query query, Map<String, Object> parameters)
    {
        try
        {
            return db.format(querySqlGenerator.getSql(query, parameters));
        }
        catch (Be5Exception e)
        {
            log.log(Level.SEVERE, "Error in queryBuilder", e);
            errorModelList.add(errorModelHelper.getErrorModel(e));
            return "";
        }
    }

    private static SqlType getSqlType(String sql)
    {
        if (sql == null || sql.trim().length() == 0) return SqlType.SELECT;
        AstStart parse;
        try
        {
            parse = SqlQuery.parse(sql);
        }
        catch (IllegalArgumentException e)
        {
            return SqlType.SELECT;
        }

        if (parse.getQuery().children().select(AstUpdate.class).findAny().isPresent())
        {
            return SqlType.UPDATE;
        }
        if (parse.getQuery().children().select(AstInsert.class).findAny().isPresent())
        {
            return SqlType.INSERT;
        }
        if (parse.getQuery().children().select(AstDelete.class).findAny().isPresent())
        {
            return SqlType.DELETE;
        }

        return SqlType.SELECT;
    }

    enum SqlType
    {
        INSERT, SELECT, UPDATE, DELETE
    }

    public static class Data
    {
        final String sql;
        final String finalSql;
        final List<String> history;

        public Data(String sql, String finalSql, List<String> history)
        {
            this.sql = sql;
            this.finalSql = finalSql;
            this.history = history;
        }

        public String getSql()
        {
            return sql;
        }

        public String getFinalSql()
        {
            return finalSql;
        }

        public List<String> getHistory()
        {
            return history;
        }
    }
}
