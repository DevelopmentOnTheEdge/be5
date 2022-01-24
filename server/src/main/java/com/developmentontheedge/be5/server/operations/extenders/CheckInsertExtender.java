package com.developmentontheedge.be5.server.operations.extenders;

import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.base.BeModelCollection;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.server.operations.support.OperationExtenderSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.FrontendConstants.SEARCH_PRESETS_PARAM;

/**
 * OperationExtender suitable to check whether operation is available.
 * Unlike CheckRecordsExtender, there's no selected records.
 * So check by presetValues in the "AllowedRecordsForInsert" view and table references to this entities.
 * If some of records absent in the given view, operation invocation will be stopped.
 *
 */
public class CheckInsertExtender extends OperationExtenderSupport
{
    private static final Logger log = Logger.getLogger(CheckInsertExtender.class.getName());
    public static final String ALLOWED_RECORDS_VIEW_INSERT = "AllowedRecordsForInsert";

    private String message;

    @Override
    public Object postGetParameters(Operation op, Object parameters, Map<String, Object> presetValues) throws Exception
    {
        boolean skip = skip(op);
        if (skip)
        {
            op.setResult(OperationResult.error(message));
        }
        return parameters;
    }

    @Override
    public boolean skipInvoke(Operation op, Object parameters)
    {
        boolean skip = skip(op);
        if (skip)
        {
            op.setResult(OperationResult.error(message));
        }
        return skip;
    }

    private boolean skip(Operation op)
    {
        Entity entity = op.getInfo().getEntity();
        BeModelCollection<Query> entityQueries = entity.getQueries();
        Query query;
        query = entityQueries.get(ALLOWED_RECORDS_VIEW_INSERT);
        if (query == null)
        {
            message = "Checked query not found for entity: " + op.getInfo().getEntityName();
            return true;
        }

        try
        {
            Map<String, Object> presetParams = op.getContext().getParams();
            Object searchPresetParams = presetParams.get(SEARCH_PRESETS_PARAM);
            if (searchPresetParams != null)
            {
                List<String> searchPresetParamsList = searchPresetParams instanceof String ?
                        Collections.singletonList((String) searchPresetParams) :
                        Arrays.asList((String[]) searchPresetParams);
                presetParams.keySet().removeIf(key -> !searchPresetParamsList.contains(key));
            }
            List<QRec> dps = queries.query(query, presetParams);

            if (dps.isEmpty())
            {
                if (userInfo.isAdmin())
                {
                    message = "Cannot execute operation " + op.getInfo().getEntityName() + "." + op.getInfo().getName() +
                            ": the following records are not found or not accessible: " + presetParams;
                }
                else
                {
                    message = userAwareMeta.getLocalizedExceptionMessage("Access to these records is denied.");
                }
                return true;
            }
        }
        catch (Throwable e)
        {
            message = "Cannot execute operation " + op.getInfo().getEntityName() + "." + op.getInfo().getName() +
                            ": " + e.getMessage();
            log.log(Level.SEVERE, message, e);
            return true;
        }

        return false;
    }
}
