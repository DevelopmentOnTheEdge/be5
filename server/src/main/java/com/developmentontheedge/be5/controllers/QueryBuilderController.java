package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Controller;
import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.sql.model.AstDelete;
import com.developmentontheedge.sql.model.AstInsert;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.AstUpdate;
import com.developmentontheedge.sql.model.SqlQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.api.SessionConstants.QUERY_BUILDER_HISTORY;


public class QueryBuilderController extends ControllerSupport implements Controller
{
    private static final String entityName = "queryBuilderComponent";

    private List<ResourceData> resourceDataList;
    private List<ErrorModel> errorModelList;

    private final SqlService db;
    private final DocumentGenerator documentGenerator;
    private final ProjectProvider projectProvider;
    private final QueryService queryService;

    @Inject
    public QueryBuilderController(SqlService db, DocumentGenerator documentGenerator, ProjectProvider projectProvider, QueryService queryService)
    {
        this.db = db;
        this.documentGenerator = documentGenerator;
        this.projectProvider = projectProvider;
        this.queryService = queryService;
    }

    @Override
    public void generate(Request req, Response res)
    {
        resourceDataList = new ArrayList<>();
        errorModelList = new ArrayList<>();

        if(UserInfoHolder.isSystemDeveloper())
        {
            String sql = req.get("sql");
            boolean execute = sql != null;

            List<String> history;
            if(req.getAttribute(QUERY_BUILDER_HISTORY) != null)
            {
                history = (List<String>) req.getAttribute(QUERY_BUILDER_HISTORY);
            }
            else
            {
                history = new ArrayList<>();
            }

            if(sql == null)
            {
                if(!history.isEmpty()){
                    sql = history.get(history.size()-1);
                }else{
                    sql = "select * from users";
                }
            }
            else
            {
                if(history.isEmpty() || !history.get(history.size()-1).equals(sql))
                {
                    history.add(sql);
                    req.setAttribute(QUERY_BUILDER_HISTORY, history);
                }
            }

            ResourceData resourceData = new ResourceData(
                    "queryBuilder",
                    new Data(sql, history),
                    Collections.singletonMap(SELF_LINK, "queryBuilder")
            );

            try{
                SqlType type = getSqlType(sql);

                if(type == SqlType.SELECT)
                {
                    select(sql, req);
                }
                else
                {
                    if(execute)
                    {
                        switch (type)
                        {
                            case INSERT:
                                insert(sql);
                                break;
                            case UPDATE:
                                update(sql);
                                break;
                            case DELETE:
                                update(sql);
                                break;
                            default:
                                res.sendUnknownActionError();
                                return;
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                errorModelList.add(new ErrorModel(Be5Exception.internal(e)));
            }

            res.sendAsJson(JsonApiModel.data(
                    resourceData,
                    errorModelList.toArray(new ErrorModel[0]),
                    resourceDataList.toArray(new ResourceData[0]),
                    req.getDefaultMeta(),
                    null
            ));
        }
        else
        {
            res.sendErrorAsJson(
                    new ErrorModel(Be5Exception.accessDenied(), "Role " + RoleType.ROLE_SYSTEM_DEVELOPER + " required.",
                            Collections.singletonMap(SELF_LINK, "queryBuilder")),
                    req.getDefaultMeta()
            );
        }
    }

    private void insert(String sql)
    {
        Object id = db.insert(sql);

        resourceDataList.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(
                        "Insert was successful",
                        "New primaryKey: " + id
                ),
                null
        ));
    }

    private void update(String sql)
    {
        Object id = db.update(sql);

        resourceDataList.add(new ResourceData(
                "result",
                FrontendConstants.STATIC_ACTION,
                new StaticPagePresentation(
                        "Update was successful",
                        id + " row(s) affected"
                ),
                null
        ));
    }

    private void select(String sql, Request req)
    {
        String userQBuilderQueryName = UserInfoHolder.getUserName() + "Query";

        Map<String, Object> parameters = req.getValuesFromJson(RestApiConstants.VALUES);

        Entity entity = new Entity( entityName, projectProvider.getProject().getApplication(), EntityType.TABLE );
        DataElementUtils.save( entity );

        Query query = new Query( userQBuilderQueryName, entity );
        query.setType(QueryType.D1_UNKNOWN);

        if(sql != null)
        {
            query.setQuery(sql);
        }
        DataElementUtils.save( query );

        try
        {
            resourceDataList.add(new ResourceData(
                    "finalSql",
                    FrontendConstants.STATIC_ACTION,
                    new StaticPagePresentation(
                            "Final sql",
                            queryService.build(query, parameters).getFinalSql()
                    ),
                    null
            ));
        }
        catch (Be5Exception e)
        {
            errorModelList.add(new ErrorModel(e));
        }

        try
        {
            JsonApiModel document = documentGenerator.getJsonApiModel(query, parameters);

            //todo refactor documentGenerator
            document.getData().setId("result");
            resourceDataList.add(document.getData());
            resourceDataList.addAll(Arrays.asList(document.getIncluded()));
        }
        catch (Be5Exception e)
        {
            errorModelList.add(new ErrorModel(e));
        }

        entity.getOrigin().remove(entityName);
    }

    private static SqlType getSqlType(String sql)
    {
        if(sql == null || sql.trim().length() == 0)return SqlType.SELECT;

        AstStart parse = SqlQuery.parse(sql);
        if(parse.getQuery().children().select(AstUpdate.class).findAny().isPresent())
        {
            return SqlType.UPDATE;
        }
        if(parse.getQuery().children().select(AstInsert.class).findAny().isPresent())
        {
            return SqlType.INSERT;
        }
        if(parse.getQuery().children().select(AstDelete.class).findAny().isPresent())
        {
            return SqlType.DELETE;
        }

        return SqlType.SELECT;
    }

    enum SqlType {
        INSERT, SELECT, UPDATE, DELETE
    }

    public class Data
    {
        String sql;
        List<String> history;

        public Data(String sql, List<String> history)
        {
            this.sql = sql;
            this.history = history;
        }

        public String getSql()
        {
            return sql;
        }

        public List<String> getHistory()
        {
            return history;
        }
    }
}
