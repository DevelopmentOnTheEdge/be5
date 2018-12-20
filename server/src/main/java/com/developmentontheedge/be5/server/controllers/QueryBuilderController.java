package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.FrontendConstants;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.server.RestApiConstants;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.sql.model.AstDelete;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstUpdate;
import com.developmentontheedge.sql.model.SqlQuery;
import com.google.inject.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.server.SessionConstants.QUERY_BUILDER_HISTORY;

@Singleton
public class QueryBuilderController extends JsonApiModelController
{
    private final DbService db;
    private final DocumentGenerator documentGenerator;
    private final Meta meta;
    private final QueryExecutorFactory queryService;
    private final ErrorModelHelper errorModelHelper;
    private final UserInfoProvider userInfoProvider;
    private final Stage stage;

    @Inject
    public QueryBuilderController(DbService db, DocumentGenerator documentGenerator, Meta meta,
                                  QueryExecutorFactory queryService, ErrorModelHelper errorModelHelper,
                                  UserInfoProvider userInfoProvider, Stage stage)
    {
        this.db = db;
        this.documentGenerator = documentGenerator;
        this.meta = meta;
        this.queryService = queryService;
        this.errorModelHelper = errorModelHelper;
        this.userInfoProvider = userInfoProvider;
        this.stage = stage;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String requestSubUrl)
    {
        List<ResourceData> includedData = new ArrayList<>();
        List<ErrorModel> errorModelList = new ArrayList<>();

        if (userInfoProvider.isSystemDeveloper())
        {
            String sql = req.get("sql");
            boolean execute = sql != null;

            List<String> history;
            if (req.getSession().get(QUERY_BUILDER_HISTORY) != null)
            {
                @SuppressWarnings("unchecked")
                List<String> historyFromSession = (List<String>) req.getSession().get(QUERY_BUILDER_HISTORY);
                history = historyFromSession;
            }
            else
            {
                history = new ArrayList<String>() {{
                    add("select * from users");
                }};
            }

            if (sql == null)
            {
                sql = history.get(history.size() - 1);
            }
            else
            {
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
                    updateUnsafe(includedData, sql);
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
                data = new Data(sql, "", history);
                errorModelList.add(errorModelHelper.getErrorModel(Be5Exception.internal(e)));
            }

            Map<String, Object> parameters = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));
            ResourceData resourceData = new ResourceData(
                    "queryBuilder",
                    data,
                    Collections.singletonMap(SELF_LINK, new HashUrl("queryBuilder").named(parameters).toString())
            );

            return data(
                    resourceData,
                    errorModelList.toArray(new ErrorModel[0]),
                    includedData.toArray(new ResourceData[0])
            );
        }
        else
        {
            return error(errorModelHelper.getErrorModel(Be5Exception.accessDenied("Role " + RoleType.ROLE_SYSTEM_DEVELOPER + " required."),
                    Collections.singletonMap(SELF_LINK, "queryBuilder")));
        }
    }

    private void insert(List<ResourceData> includedData, String sql)
    {
        Object id = db.insert(sql);

        includedData.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(
                        "Insert was successful",
                        "New primaryKey: " + id
                ),
                null
        ));
    }

    private void update(List<ResourceData> includedData, String sql)
    {
        Object id = db.update(sql);

        includedData.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(
                        "Update was successful",
                        id + " row(s) affected"
                ),
                null
        ));
    }

    private void updateUnsafe(List<ResourceData> includedData, String sql)
    {
        Object id = db.updateUnsafe(sql);

        includedData.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(
                        "Update was successful",
                        id + " row(s) affected"
                ),
                null
        ));
    }

    private String select(List<ResourceData> includedData, List<ErrorModel> errorModelList, String sql, Request req)
    {
        Map<String, Object> parameters = ParseRequestUtils.getValuesFromJson(req.get(RestApiConstants.VALUES));
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
            if (stage == Stage.DEVELOPMENT) log.log(Level.SEVERE, "Error in queryBuilder", e);
            errorModelList.add(errorModelHelper.getErrorModel(e));
        }
        return finalSql;
    }

    private String getFinalSql(List<ErrorModel> errorModelList, Query query, Map<String, Object> parameters)
    {
        try
        {
            return queryService.build(query, parameters).getFinalSql().getQuery().toString();
        }
        catch (Be5Exception e)
        {
            if (stage == Stage.DEVELOPMENT) log.log(Level.SEVERE, "Error in queryBuilder", e);
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
