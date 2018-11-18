package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.services.events.Be5EventLogger;
import com.developmentontheedge.be5.server.services.events.EventManager;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_OPERATION;
import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_QUERY;

public class Be5EventDbLogger implements Be5EventLogger
{
    public static final Logger log = Logger.getLogger(Be5EventDbLogger.class.getName());

    private static final String EVENT_LOG_TABLE = "be5events";
    private static final String EVENT_LOG_TABLE_PARAMS = "be5eventParams";
    private final UserInfoProvider userInfoProvider;
    private final DatabaseModel database;

    @Inject
    public Be5EventDbLogger(EventManager eventManager, DatabaseModel database,
                            ProjectProvider projectProvider, UserInfoProvider userInfoProvider)
    {
        this.database = database;
        this.userInfoProvider = userInfoProvider;
        if (projectProvider.get().hasFeature(Features.EVENT_DB_LOGGING_FEATURE))
        {
            eventManager.addListener(this);
        }
    }

    @Override
    public void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime)
    {
        storeRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_QUERY, query.getEntity().getName(), query.getName(), parameters, null);
    }

    @Override
    public void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception)
    {
        storeErrorRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_QUERY, query.getEntity().getName(), query.getName(), parameters, exception);
    }

    @Override
    public void operationCompleted(Operation operation, Map<String, Object> values, long startTime, long endTime)
    {
        storeRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_OPERATION,
                operation.getInfo().getEntityName(), operation.getInfo().getName(),
                operation.getContext().getOperationParams(), null);
    }

    @Override
    public void operationError(Operation operation, Map<String, Object> values, long startTime, long endTime, String exception)
    {
        storeErrorRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(),
                startTime, endTime, ACTION_OPERATION,
                operation.getInfo().getEntityName(), operation.getInfo().getName(),
                operation.getContext().getOperationParams(), exception);
    }

    private void storeErrorRecord(String user_name, String remoteAddr, long startTime, long endTime,
                             String action, String entity, String title, Map<String, Object> parameters, String exception)
    {
        Long id = database.getEntity(EVENT_LOG_TABLE).add(new HashMap<String, Object>() {{
            put("user_name", user_name);
            put("IP", remoteAddr);
            put("startTime", new Timestamp(startTime));
            put("endTime", new Timestamp(endTime));
            put("action", action);
            put("entity", entity);
            put("title", title);
            put("exception", exception);
        }});
        storeParams(parameters, id);
    }

    private void storeRecord(String user_name, String remoteAddr, long startTime, long endTime,
                                 String action, String entity, String title, Map<String, Object> parameters, String result)
    {
        Long id = database.getEntity(EVENT_LOG_TABLE).add(new HashMap<String, Object>() {{
            put("user_name", user_name);
            put("IP", remoteAddr);
            put("startTime", new Timestamp(startTime));
            put("endTime", new Timestamp(endTime));
            put("action", action);
            put("entity", entity);
            put("title", title);
            put("result", result);
        }});
        storeParams(parameters, id);
    }

    private void storeParams(Map<String, Object> parameters, Long id)
    {
        for (Map.Entry<String, Object> param : parameters.entrySet())
        {
            database.getEntity(EVENT_LOG_TABLE_PARAMS).add(new HashMap<String, Object>() {{
                put("logID", id);
                put("paramName", param.getKey());
                put("paramValue", param.getValue());
            }});
        }
    }
}
