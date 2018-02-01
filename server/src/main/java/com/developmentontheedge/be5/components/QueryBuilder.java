package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.env.Injector;
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
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        if(UserInfoHolder.isSystemDeveloper())
        {
            Entity e = new Entity( "queryBuilderComponent", injector.getProject().getApplication(), EntityType.TABLE );
            DataElementUtils.save( e );
            Query query = new Query( "query", e );
            DataElementUtils.save( query );
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
