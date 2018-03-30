package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.FrontendConstants;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.query.DocumentGenerator;
import com.developmentontheedge.be5.query.impl.model.Be5QueryExecutor;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class QueryBuilder implements Component
{
    private static final String entityName = "queryBuilderComponent";

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        DocumentGenerator documentGenerator = injector.get(DocumentGenerator.class);

        String userQBuilderQueryName = UserInfoHolder.getUserName() + "Query";

        if(UserInfoHolder.isSystemDeveloper())
        {
            Map<String, String> parametersMap = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);

            Entity entity = injector.getMeta().findEntity(entityName).orElseGet(() -> {
                Entity e = new Entity( entityName, injector.getProject().getApplication(), EntityType.TABLE );
                DataElementUtils.save( e );
                return e;
            });

            Query query = injector.getMeta().findQuery(entityName, userQBuilderQueryName).orElseGet(() -> {
                Query q = new Query( userQBuilderQueryName, entity );
                q.setType(QueryType.D1_UNKNOWN);
                q.setQuery("select * from users");
                return q;
            });

            if(req.get("sql") != null)
            {
                query.setQuery(req.get("sql"));
            }
            DataElementUtils.save( query );

            ResourceData resourceData = new ResourceData("queryBuilder", query.getQuery(), Collections.singletonMap(SELF_LINK, "queryBuilder"));
            List<ResourceData> resourceDataList = new ArrayList<>();
            List<ErrorModel> errorModelList = new ArrayList<>();

            try
            {
                resourceDataList.add(new ResourceData(
                    "finalSql",
                    FrontendConstants.STATIC_ACTION,
                    new StaticPagePresentation(
                            "Final sql",
                            new Be5QueryExecutor(query, parametersMap, injector).getFinalSql()
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
                JsonApiModel document = documentGenerator.getDocument(query, parametersMap);

                //todo refactor documentGenerator
                document.getData().setId("queryTable");
                resourceDataList.add(document.getData());
                resourceDataList.addAll(Arrays.asList(document.getIncluded()));
            }
            catch (Be5Exception e)
            {
                errorModelList.add(new ErrorModel(e));
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
}
