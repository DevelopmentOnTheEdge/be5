package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationSupport;

import java.util.Map;


public class DeleteOperation extends OperationSupport implements Operation
{

    @Override
    public Object getParameters(Map<String, String> presetValues) throws Exception
    {
        return null;
    }

    @Override
    public void invoke(Object parameters, OperationContext context) throws Exception {
        for (long id : context.getRecordIDs()){
            String deleteSql = getDeleteSql(getInfo().getEntity().getName());
            db.update("delete from "+getInfo().getEntity().getName()+" where id = ?", id);

            //TODOdb.update(deleteSql, id);
        }
    }

    private String getDeleteSql(String table) throws Exception
    {
        String delSql = "DELETE FROM " + table;
        getInfo().getEntity().getScheme();

//        if( Utils.columnExists( connector, table, IS_DELETED_COLUMN_NAME ) )
//        {
//            delSql = "UPDATE " + tName + " SET " + IS_DELETED_COLUMN_NAME + " = 'yes'";
//            if( Utils.columnExists( connector, table, WHO_MODIFIED_COLUMN_NAME ) )
//            {
//                delSql += ", " + WHO_MODIFIED_COLUMN_NAME + " = " + Utils.safestr( connector, userInfo.getUserName(), true );
//            }
//            if( Utils.columnExists( connector, table, MODIFICATION_DATE_COLUMN_NAME ) )
//            {
//                delSql += ", " + MODIFICATION_DATE_COLUMN_NAME + " = " + analyzer.getCurrentDateTimeExpr();
//            }
//        }
//
//        if( dryRun )
//        {
//            delSql = "SELECT " + analyzer.quoteIdentifier( Utils.findPrimaryKeyName( connector, table ) ) + " FROM " + tName;
//        }

        return delSql;
    }
}
