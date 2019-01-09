package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.lifecycle.Start;
import com.developmentontheedge.be5.base.meta.ProjectProvider;
import com.developmentontheedge.be5.base.security.UserInfoProvider;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.metadata.Features;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.services.events.Be5EventLogger;
import com.developmentontheedge.be5.server.services.events.EventManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_OPERATION;
import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_QUERY;
import static com.developmentontheedge.be5.server.services.events.EventManager.ACTION_SERVLET;

public class Be5EventDbLogger implements Be5EventLogger
{
    public static final Logger log = Logger.getLogger(Be5EventDbLogger.class.getName());

    private static final String EVENT_LOG_TABLE = "be5events";
    private static final String EVENT_LOG_TABLE_PARAMS = "be5eventParams";
    private final UserInfoProvider userInfoProvider;
    private final DatabaseModel database;
    private final EventManager eventManager;
    private final ProjectProvider projectProvider;
    private final Provider<HttpServletRequest> request;

    @Inject
    public Be5EventDbLogger(EventManager eventManager, DatabaseModel database, ProjectProvider projectProvider,
                            UserInfoProvider userInfoProvider, Provider<HttpServletRequest> request)
    {
        this.database = database;
        this.userInfoProvider = userInfoProvider;
        this.eventManager = eventManager;
        this.projectProvider = projectProvider;
        this.request = request;
    }

    @Start(order = 30)
    public void start() throws Exception
    {
        if (projectProvider.get().hasFeature(Features.EVENT_DB_LOGGING_FEATURE))
        {
            eventManager.addListener(this);
        }
    }

    @Override
    public void queryCompleted(Query query, Map<String, Object> parameters, long startTime, long endTime)
    {
        storeRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(), startTime, endTime,
                ACTION_QUERY, query.getEntity().getName(), query.getName(), parameters, null);
    }

    @Override
    public void queryError(Query query, Map<String, Object> parameters, long startTime, long endTime, String exception)
    {
        storeErrorRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(), startTime, endTime,
                ACTION_QUERY, query.getEntity().getName(), query.getName(), parameters, null, exception);
    }

    @Override
    public void operationCompleted(Operation operation, Map<String, Object> values, long startTime, long endTime)
    {
        storeRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(), startTime, endTime,
                ACTION_OPERATION, operation.getInfo().getEntityName(), operation.getInfo().getName(),
                operation.getContext().getParams(), operation.getStatus().toString());
    }

    @Override
    public void operationError(Operation operation, Map<String, Object> values, long startTime,
                               long endTime, String exception)
    {
        storeErrorRecord(userInfoProvider.getUserName(), userInfoProvider.getRemoteAddr(), startTime, endTime,
                ACTION_OPERATION, operation.getInfo().getEntityName(), operation.getInfo().getName(),
                operation.getContext().getParams(), operation.getStatus().toString(), exception);
    }

    @Override
    public void servletCompleted(String servletName, String requestUri, Map<String, ?> params,
                                 long startTime, long endTime)
    {
        HttpSession session = request.get().getSession(false);
        storeRecord(getUserName(session), request.get().getRemoteAddr(), startTime, endTime,
                ACTION_SERVLET, servletName, requestUri, params, "");
    }

    @Override
    public void servletError(String servletName, String requestUri, Map<String, ?> params,
                             long startTime, long endTime, String exception)
    {
        HttpSession session = request.get().getSession(false);
        storeErrorRecord(getUserName(session), request.get().getRemoteAddr(), startTime, endTime,
                ACTION_SERVLET, servletName, requestUri, params, "", exception);
    }

    private String getUserName(HttpSession session)
    {
        if (session == null)
        {
            return "Guest";
        }
        else
        {
            return userInfoProvider.getUserName();
        }
    }

    private void storeErrorRecord(String user_name, String remoteAddr, long startTime, long endTime, String action,
                              String entity, String title, Map<String, ?> parameters, String result, String exception)
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
            put("exception", exception);
        }});
        storeParams(parameters, id);
    }

    private void storeRecord(String user_name, String remoteAddr, long startTime, long endTime, String action,
                             String entity, String title, Map<String, ?> parameters, String result)
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

    private void storeParams(Map<String, ?> parameters, Long id)
    {
        for (Map.Entry<String, ?> param : parameters.entrySet())
        {
            if (param.getValue() instanceof String[])
            {
                String[] values = (String[]) param.getValue();
                for (String value : values)
                {
                    addParam(id, param.getKey(), value);
                }
            }
            else
            {
                addParam(id, param.getKey(), param.getValue().toString());
            }
        }
    }

    private void addParam(Long id, String name, String value)
    {
        database.getEntity(EVENT_LOG_TABLE_PARAMS).add(new HashMap<String, Object>() {{
            put("logID", id);
            put("paramName", name);
            put("paramValue", value);
        }});
    }
}
