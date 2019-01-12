package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.be5.databasemodel.helpers.SqlHelper;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.IS_DELETED_COLUMN_NAME;
import static com.developmentontheedge.be5.metadata.model.EntityType.COLLECTION;
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION;
import static java.util.Collections.singletonMap;

public class RestoreOperation extends OperationSupport implements TransactionalOperation
{
    protected StringBuilder out = new StringBuilder();
    @Inject private SqlHelper sqlHelper;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        if (meta.getColumn(getInfo().getEntityName(), IS_DELETED_COLUMN_NAME) == null)
        {
            setResult(OperationResult.error("Entity does not support record recovery"));
        }
        return null;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        String entityName = getInfo().getEntityName();
        int updateCount = sqlHelper.updateIn(entityName, getInfo().getPrimaryKey(), context.getRecords(),
                singletonMap(IS_DELETED_COLUMN_NAME, "no"));

        out.append("" + updateCount + " " + ("records were restored from") +
                " <i>" + getInfo().getEntityName() + "</i><br />");

        List<TableReference> collectionRefs = meta.getTableReferences(COLLECTION);

        for (TableReference reference : collectionRefs)
        {
            if (getInfo().getEntityName().equals(reference.getTableTo()) &&
                    getInfo().getEntity().getPrimaryKey().equalsIgnoreCase(reference.getColumnsTo()) &&
                    meta.getColumn(reference.getTableFrom(), IS_DELETED_COLUMN_NAME) != null)
            {
                int updateCount1 = sqlHelper.updateIn(reference.getTableFrom(), reference.getColumnsFrom(),
                        context.getRecords(),
                        singletonMap(IS_DELETED_COLUMN_NAME, "no"));

                if (updateCount1 > 0)
                {
                    out.append("" + updateCount1 +
                            " " + ("records were restored from the collection") +
                            " <i>" + reference.getTableFrom() + "</i><br />");
                }
            }
        }

        if (!GENERIC_COLLECTION.equals(getInfo().getEntity().getType()))
        {
            List<TableReference> genericCollectionRefs = meta.getTableReferences(GENERIC_COLLECTION);

            for (TableReference reference : genericCollectionRefs)
            {
                if (reference.getColumnsTo() == null &&
                        meta.getColumn(reference.getTableFrom(), IS_DELETED_COLUMN_NAME) != null)
                {
                    int updateCount1 = sqlHelper.updateIn(reference.getTableFrom(), reference.getColumnsFrom(),
                            Utils.addPrefix(getInfo().getEntityName() + ".", context.getRecords()),
                            singletonMap(IS_DELETED_COLUMN_NAME, "no"));

                    if (updateCount1 > 0)
                    {
                        out.append("" + updateCount1 +
                                " " + ("records were restored from the generic collection") +
                                " <i>" + reference.getTableFrom() + "</i><br />");
                    }
                }
            }
        }

        if (userInfo.getCurrentRoles().contains(RoleType.ROLE_SYSTEM_DEVELOPER))
        {
            setResult(OperationResult.finished(out.toString()));
        }
        else
        {
            setResult(OperationResult.finished());
        }
    }
}
