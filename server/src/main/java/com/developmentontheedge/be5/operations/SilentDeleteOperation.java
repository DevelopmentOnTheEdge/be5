package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.util.Utils;

import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.model.EntityType.COLLECTION;
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION;


public class SilentDeleteOperation extends OperationSupport
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        //todo add redirect params
        return null;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        database.getEntity(getInfo().getEntityName()).remove(context.records);

        List<TableReference> collectionRefs = meta.getRefToTable(COLLECTION, getInfo().getEntityName());

        for (TableReference reference : collectionRefs)
        {
            db.update("DELETE FROM " + reference.getTableFrom() +
                     " WHERE " + reference.getColumnsFrom() + " IN " + Utils.inClause(context.records.length),
                     (Object[]) context.records);
        }

        List<TableReference> genericCollectionRefs = meta.getRefToTable(GENERIC_COLLECTION, getInfo().getEntityName());

        for (TableReference reference : genericCollectionRefs)
        {
            db.update("DELETE FROM " + reference.getTableFrom() +
                     " WHERE " + reference.getColumnsFrom() + " IN " + Utils.inClause(context.records.length),
                     (Object[]) Utils.addPrefix(context.records, getInfo().getEntityName()));
        }

    }
}
