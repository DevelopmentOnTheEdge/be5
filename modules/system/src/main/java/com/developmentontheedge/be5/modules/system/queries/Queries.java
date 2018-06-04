package com.developmentontheedge.be5.modules.system.queries;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;
import com.developmentontheedge.be5.server.util.ActionUtils;

import java.util.ArrayList;
import java.util.List;


public class Queries extends TableBuilderSupport
{
    @Override
    public TableModel getTableModel()
    {
        addColumns("EntityName", "Name", "Type", "Roles", "Operations");

        String selectEntity = (String)parameters.get("entity");

        if(selectEntity != null)
        {
            addQueries(selectEntity);
        }
        else
        {
            meta.getOrderedEntities(userInfo.getLanguage()).forEach(
                    e -> addQueries(e.getName())
            );
        }

        return table(columns, rows);
    }


    public void addQueries(String entityName)
    {
        List<String> queries = meta.getQueryNames(meta.getEntity(entityName));
        for (String queryName : queries)
        {
            Query query = meta.getQuery(entityName, queryName);
            List<CellModel> cells = new ArrayList<CellModel>();

            cells.add(new CellModel(entityName));
            cells.add(new CellModel(query.getName()).add("link", "url", ActionUtils.toAction(query).getArg()));
            cells.add(new CellModel(query.getType()));
            cells.add(new CellModel(query.getRoles().getFinalRoles().toString()));
            cells.add(new CellModel(query.getOperationNames().getFinalValues().size()));

            addRow(cells);
        }

    }

}
