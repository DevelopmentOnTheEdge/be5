package com.developmentontheedge.be5.modules.core.queries.system.meta;

import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.support.Cell;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;
import com.developmentontheedge.be5.server.util.ActionUtils;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.List;

import static com.developmentontheedge.be5.FrontendConstants.TABLE_ACTION;


public class EntitiesTable extends QueryExecutorSupport
{
    @Override
    public List<QRec> execute()
    {
        addColumns("Name", "Type", "Columns", "Queries", "Operations");

        List<Entity> entities = meta.getOrderedEntities(userInfo.getLanguage());
        for (Entity entity : entities)
        {
            Cell name = cell(entity.getName());
            Query allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW);
            if (allRecords != null) name.link(ActionUtils.toAction(allRecords).getArg());

            addRow(cells(name,
                    entity.getTypeString(),
                    meta.getColumns(entity).size(),

                    cell(meta.getQueryNames(entity).size())
                            .link(new HashUrl(TABLE_ACTION, "_system_", "Queries")
                                            .named("entity", entity.getName()).toString()),

                    cell(meta.getOperationNames(entity).size())
                            .link(new HashUrl(TABLE_ACTION, "_system_", "Operations")
                                            .named("entity", entity.getName()).toString())
            ));
        }
        return table();
    }
}
