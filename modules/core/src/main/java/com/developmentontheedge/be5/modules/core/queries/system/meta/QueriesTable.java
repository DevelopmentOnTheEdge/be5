package com.developmentontheedge.be5.modules.core.queries.system.meta;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;
import com.developmentontheedge.be5.server.util.ActionUtils;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;


public class QueriesTable extends QueryExecutorSupport
{
    @Override
    public List<DynamicPropertySet> execute()
    {
        addColumns("EntityName", "Name", "Type", "Roles", "Operations");

        String selectEntity = (String) parameters.get("entity");
        if (selectEntity != null)
        {
            addQueries(selectEntity);
        }
        else
        {
            meta.getOrderedEntities(userInfo.getLanguage()).forEach(
                    e -> addQueries(e.getName())
            );
        }
        return table();
    }

    public void addQueries(String entityName)
    {
        List<String> queries = meta.getQueryNames(meta.getEntity(entityName));
        for (String queryName : queries)
        {
            Query query = meta.getQuery(entityName, queryName);
            addRow(cells(
                    entityName,
                    cell(query.getName())
                            .option("link", "url", ActionUtils.toAction(query).getArg()),
                    query.getType(),
                    query.getRoles().getFinalRoles().toString(),
                    query.getOperationNames().getFinalValues().size()
            ));
        }
    }
}
