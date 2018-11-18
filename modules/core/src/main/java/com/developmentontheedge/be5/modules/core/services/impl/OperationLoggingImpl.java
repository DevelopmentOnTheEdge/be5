package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.DateUtils;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.operation.util.OperationUtils.operationSuccessfullyFinished;

public class OperationLoggingImpl implements OperationLogging
{
    private static final Logger log = Logger.getLogger(OperationLoggingImpl.class.getName());
    private static final String OPERATION_LOGS_TABLE = "be5operationLogs";
    private static final String OPERATION_LOGS_PARAMS_TABLE = "be5operationLogParams";

    private final ProjectProvider projectProvider;
    private final UserInfoProvider userInfoProvider;
    private final Provider<Session> session;
    private final DatabaseModel database;
    private final DbService db;

    @Inject
    public OperationLoggingImpl(ProjectProvider projectProvider, UserInfoProvider userInfoProvider,
                                Provider<Session> session, DatabaseModel database, DbService db)
    {
        this.projectProvider = projectProvider;
        this.userInfoProvider = userInfoProvider;
        this.session = session;
        this.database = database;
        this.db = db;
    }

    public void saveOperationLog(Operation operation, Map<String, Object> values)
    {
        if (projectProvider.get().hasFeature(Features.LOGGING) &&
                operationSuccessfullyFinished(operation.getStatus()))
        {
            Long id = database.getEntity(OPERATION_LOGS_TABLE).add(new HashMap<String, Object>()
            {{
                put("table_name", operation.getInfo().getEntityName());
                put("operation_name", operation.getInfo().getName());
                put("user_name", userInfoProvider.getUserName());
                put("executedAt", DateUtils.currentTimestamp());
                put("remoteAddr", userInfoProvider.getRemoteAddr());
                put("result", operation.getResult().getMessage());
            }});
            saveExecutionParametersReal(id, operation, values);
        }
    }

    private void saveExecutionParametersReal(Long id, Operation operation, Map<String, Object> values)
    {
        saveSessionVars(id);
        saveContextParams(id, operation.getContext().getOperationParams());
        saveInputValues(id, values);
    }

    private void saveInputValues(Long id, Map<String, Object> values)
    {
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            saveVar(id, "input", entry.getKey(), entry.getValue());
        }
    }

    private void saveContextParams(Long id, Map<String, Object> contextParams)
    {
        for (Map.Entry<String, Object> entry : contextParams.entrySet())
        {
            saveVar(id, "context", entry.getKey(), entry.getValue());
        }
    }

    private void saveSessionVars(Long id)
    {
        for (Map.Entry<String, Object> entry : session.get().getAttributes().entrySet())
        {
            saveVar(id, "session", entry.getKey(), entry.getValue());
        }
    }

    private void saveVar(Long operLogID, String type, String paramName, Object paramValue)
    {
        String value = paramValue == null ? null : paramValue.toString();
        db.insert("INSERT INTO " + OPERATION_LOGS_PARAMS_TABLE +
                        " (operLogID, type, paramName, paramValue) VALUES (?, ?, ?, ?)",
                operLogID, type, paramName, value);
    }
}
