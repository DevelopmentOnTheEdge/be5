package com.developmentontheedge.be5.modules.core.queries.system.meta;

import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.server.queries.support.QueryBuilderSupport;
import com.developmentontheedge.be5.server.util.ActionUtils;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;

import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;


public class EntitiesTable extends QueryBuilderSupport
{
    @Override
    public List<DynamicPropertySet> execute()
    {
        addColumns("Name", "Type", "Columns", "Queries", "Operations");

        List<Entity> entities = meta.getOrderedEntities(userInfo.getLanguage());
        for (Entity entity : entities)
        {
            CellModel name = cell(entity.getName());
            Query allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW);
            if (allRecords != null) name.option("link", "url", ActionUtils.toAction(allRecords).getArg());

            addRow(cells(name,
                    entity.getTypeString(),
                    meta.getColumns(entity).size(),

                    cell(meta.getQueryNames(entity).size())
                            .option("link", "url",
                                    new HashUrl(TABLE_ACTION, "_system_", "Queries")
                                            .named("entity", entity.getName()).toString()),

                    cell(meta.getOperationNames(entity).size())
                            .option("link", "url",
                                    new HashUrl(TABLE_ACTION, "_system_", "Operations")
                                            .named("entity", entity.getName()).toString())
            ));
        }
        return table();
    }
}
