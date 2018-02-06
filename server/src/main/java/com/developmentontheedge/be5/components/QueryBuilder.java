package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.impl.model.Be5QueryExecutor;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.components.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;


public class QueryBuilder implements Component
{
    private static final String entityName = "queryBuilderComponent";

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        String userQBuilderQueryName = UserInfoHolder.getUserName() + "Query";

        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("sql", String.class, ""));

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

            Object table;

            dps.setValue("sql", query.getQuery());

            try
            {
                table = injector.get(DocumentGenerator.class).getTable(query, parametersMap);

                dps.add(new DynamicProperty("finalSql", String.class,
                        new Be5QueryExecutor(query, parametersMap, injector).getFinalSql()));

                res.sendAsJson(
                        new ResourceData(TABLE_ACTION, table),
                        new ResourceData[]{new ResourceData("dps", JsonFactory.dpsValues(dps))},
                        Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                        Collections.singletonMap(SELF_LINK, "qBuilder")
                );
            }
            catch (Be5Exception e)
            {
                res.sendErrorAsJson(
                        new ErrorModel(e),
                        new ResourceData[]{new ResourceData("dps", JsonFactory.dpsValues(dps))},
                        Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                        Collections.singletonMap(SELF_LINK, "qBuilder")
                );
            }

        }
        else
        {
            res.sendErrorAsJson(
                    new ErrorModel("403", "Role " + RoleType.ROLE_SYSTEM_DEVELOPER + " required."),
                    new ResourceData[]{new ResourceData("dps", JsonFactory.dpsValues(dps))},
                    Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                    Collections.singletonMap(SELF_LINK, "qBuilder")
            );
        }

    }
}
