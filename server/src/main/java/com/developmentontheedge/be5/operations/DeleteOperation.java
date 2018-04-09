package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationSupport;
import com.developmentontheedge.be5.operation.TransactionalOperation;
import com.developmentontheedge.be5.util.Utils;

import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.model.EntityType.COLLECTION;
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION;


public class DeleteOperation extends OperationSupport implements TransactionalOperation
{
    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        StringBuilder out = new StringBuilder();

        int updateCount = database.getEntity(getInfo().getEntityName()).remove(context.records);

        out.append( "" + updateCount + " " + ( "records were deleted from" ) + " <i>" + getInfo().getEntityName() + "</i><br />" );

        List<TableReference> collectionRefs = meta.getTableReferences(COLLECTION);

        for (TableReference reference : collectionRefs)
        {
            if(getInfo().getEntityName().equals(reference.getTableTo()) && getInfo().getEntity().getPrimaryKey().equals(reference.getColumnsTo()))
            {
                //TODO use utils - DELETE or UPDATE IS_DELETED_COLUMN_NAME
                int updateCount1 = db.update("DELETE FROM " + reference.getTableFrom() +
                                " WHERE " + reference.getColumnsFrom() + " IN " + Utils.inClause(context.records.length),
                        (Object[]) context.records);

                if (updateCount1 > 0)
                {
                    //todo localizedMessage
                    out.append("" + updateCount1 +
                            " " + ("records were deleted from the collection") + " <i>" + reference.getTableFrom() + "</i><br />");
                }
            }
        }

        if( !GENERIC_COLLECTION.equals( getInfo().getEntity().getType() ) )
        {
            List<TableReference> genericCollectionRefs = meta.getTableReferences(GENERIC_COLLECTION);

            for (TableReference reference : genericCollectionRefs)
            {
                if(reference.getColumnsTo() == null)
                {
                    //TODO use utils - DELETE or UPDATE IS_DELETED_COLUMN_NAME
                    int updateCount1 = db.update("DELETE FROM " + reference.getTableFrom() +
                                    " WHERE " + reference.getColumnsFrom() + " IN " + Utils.inClause(context.records.length),
                            (Object[]) Utils.addPrefix(context.records, getInfo().getEntityName() + "."));

                    if (updateCount1 > 0)
                    {
                        out.append("" + updateCount1 +
                                " " + ("records were deleted from the generic collection") + " <i>" + reference.getTableFrom() + "</i><br />");
                    }
                }
            }
        }

        setResult(OperationResult.finished(out.toString()));
    }
}
