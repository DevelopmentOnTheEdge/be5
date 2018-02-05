package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.DataElementUtils;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.EntityType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;

import java.util.Collections;

import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;


public class QueryBuilder implements Component
{
    private static final String entityName = "queryBuilderComponent";

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        String userQBuilderQueryName = UserInfoHolder.getUserName() + "Query";
        if(UserInfoHolder.isSystemDeveloper())
        {
            Entity entity = injector.getMeta().findEntity(entityName).orElseGet(() -> {
                Entity e = new Entity( entityName, injector.getProject().getApplication(), EntityType.TABLE );
                DataElementUtils.save( e );
                return e;
            });

            Query query = injector.getMeta().findQuery(entityName, userQBuilderQueryName).orElseGet(() -> {
                Query q = new Query( userQBuilderQueryName, entity );
                q.setType(QueryType.D1_UNKNOWN);
                DataElementUtils.save( q );
                return q;
            });

            query.setQuery( req.getNonEmpty("sql") );

            Object table = injector.get(DocumentGenerator.class).getTable(query, Collections.emptyMap());
            Document.sendQueryResponseData(req, res, query, table);
        }
        else
        {
            res.sendErrorAsJson(
                    new ErrorModel("403", "Role " + RoleType.ROLE_SYSTEM_DEVELOPER + " required."),
                    Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                    Collections.singletonMap(SELF_LINK, "qBuilder")
            );
        }

    }
}
