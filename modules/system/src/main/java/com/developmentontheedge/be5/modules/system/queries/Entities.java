package com.developmentontheedge.be5.modules.system.queries;

import com.developmentontheedge.be5.base.FrontendConstants;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.queries.support.TableBuilderSupport;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.util.ActionUtils;
import com.developmentontheedge.be5.base.util.HashUrl;

import java.util.ArrayList;
import java.util.List;


public class Entities extends TableBuilderSupport
{
    @Override
    public TableModel getTableModel()
    {
        addColumns("Name", "Type", "Columns", "Queries", "Operations");

        List<Entity> entities = meta.getOrderedEntities(userInfo.getLanguage());
        for (Entity entity : entities)
        {
            List<CellModel> cells = new ArrayList<CellModel>();

            CellModel name = new CellModel(entity.getName());
            Query allRecords = entity.getQueries().get(DatabaseConstants.ALL_RECORDS_VIEW);
            if (allRecords != null) name.add("link", "url", ActionUtils.toAction(allRecords).getArg());

            cells.add(name);
            cells.add(new CellModel(entity.getTypeString()));
            cells.add(new CellModel(meta.getColumns(entity).size()));

            cells.add(new CellModel(meta.getQueryNames(entity).size())
                    .add("link", "url",
                            new HashUrl(FrontendConstants.TABLE_ACTION, "_system_", "Queries")
                                .named("entity", entity.getName()).toString()));

            cells.add(new CellModel(meta.getOperationNames(entity).size()));

            addRow(cells);
        }


        return table(columns, rows);
    }

}