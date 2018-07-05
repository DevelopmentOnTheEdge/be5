package com.developmentontheedge.be5.server.operations.extenders;

import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * OperationExtender suitable to check whether records supplied for the operation appear in the
 * special view. First "AllowedRecordsFor"+operation name is checked (for example, "AllowedRecordsForEdit"),
 * then simply "AllowedRecords". Thus you can make common view for all operations and then create special view
 * for some specific operations which should be filtered in different way.
 * If some of records absent in the given view, operation invocation will be stopped.
 * @author lan
 */
public class CheckRecordsExtender extends OperationExtenderSupport
{
    public static final String ALLOWED_RECORDS_VIEW = "AllowedRecords";
    public static final String ALLOWED_RECORDS_VIEW_PREFIX = "AllowedRecordsFor";

    private String message;

    @Override
    public void getParameters(MessageHandler output, Operation op, DatabaseConnector connector, DynamicPropertySet parameters,
                              Map presetValues) throws Exception
    {
        if(skipInvoke(op, connector))
        {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public boolean skipInvoke(Operation op, DatabaseConnector connector)
    {
        String entity = op.getEntity();
        String queryId;
        queryId = Utils.readQueryID(connector, entity, ALLOWED_RECORDS_VIEW_PREFIX+op.getName());
        if(queryId == null)
        {
            queryId = Utils.readQueryID(connector, entity, ALLOWED_RECORDS_VIEW);
        }
        if(queryId == null)
        {
            return false;
        }
        String[] recordIDs = op.getRecordIDs();
        QueryExecuter qe = new QueryExecuter(connector, op.getUserInfo(), Utils.createSessionAdapter( Collections.EMPTY_MAP ),
                Collections.EMPTY_MAP, null);
        try
        {
            String pk = Utils.findPrimaryKeyName(connector, entity);
            Set<String> disabledRecords = new HashSet<String>(Arrays.asList(recordIDs));
            qe.makeIterator(queryId, Collections.singletonMap(pk, recordIDs));
            String[][] vals = qe.calcUsingQuery();
            for(String[] row: vals)
            {
                disabledRecords.remove(row[0]);
            }
            if(disabledRecords.size() > 0)
            {
                message = "Cannot execute operation " + op.getName() + ": the following records are not found or not accessible: " + disabledRecords;
                return true;
            }
        }
        catch( Exception e )
        {
            message = "Cannot execute operation " + op.getName() + ": "+e.getMessage();
            return true;
        }
        finally
        {
            qe.closeIterator();
        }
        return false;
    }

    @Override
    public String getSkipInvokeReason(Operation op, DatabaseConnector connector)
    {
        return message;
    }
}
