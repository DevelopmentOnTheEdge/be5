package com.developmentontheedge.be5.query.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.query.impl.SqlTableBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryService;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

import static com.developmentontheedge.be5.query.TableConstants.LIMIT;
import static com.developmentontheedge.be5.query.TableConstants.OFFSET;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_DIR;


public class TableModelServiceImpl implements TableModelService
{
    private static final String QUERY_POSITIONS = "QUERY_POSITIONS";
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final QueryService queryService;
    private final UserInfoProvider userInfoProvider;
    private final Provider<QuerySession> session;

    @Inject
    public TableModelServiceImpl(UserAwareMeta userAwareMeta, CoreUtils coreUtils, GroovyRegister groovyRegister,
                                 Injector injector, QueryService queryService, UserInfoProvider userInfoProvider,
                                 Provider<QuerySession> session)
    {
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;
        this.queryService = queryService;
        this.userInfoProvider = userInfoProvider;
        this.session = session;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TableModel getTableModel(Query query, Map<String, ?> parameters)
    {
        try
        {
            switch (query.getType())
            {
                case D1:
                case D1_UNKNOWN:
                    return getSqlTableModel(query, (Map<String, Object>) parameters);
                case JAVA:
                case GROOVY:
                    return getFromTableBuilder(query, (Map<String, Object>) parameters);
                default:
                    throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
            }
        }
        catch (Throwable e)
        {
            throw Be5Exception.internalInQuery(query, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SqlTableBuilder builder(Query query, Map<String, ?> parameters)
    {
        return new SqlTableBuilder(query, (Map<String, Object>) parameters, userInfoProvider.get(), queryService, userAwareMeta);
    }

    private TableModel getSqlTableModel(Query query, Map<String, Object> parameters)
    {
        Map<String, String> positions = getUserQueryPositions(query);
        int orderColumn = Integer.parseInt(getPosition(parameters, positions, ORDER_COLUMN, "-1"));
        String orderDir = getPosition(parameters, positions, ORDER_DIR, "asc");
        int offset = Integer.parseInt(getPosition(parameters, positions, OFFSET, "0"));
        int limit = Integer.parseInt(getPosition(parameters, positions, LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            //todo move defaultPageLimit, to getQuerySettings(query)
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        return new SqlTableBuilder(query, parameters, userInfoProvider.get(), queryService, userAwareMeta)
                .sortOrder(orderColumn, orderDir)
                .offset(offset)
                .limit(Math.min(limit, maxLimit))
                .build();
    }

    private String getPosition(Map<String, Object> parameters, Map<String, String> positions, String name, String defaultValue)
    {
        if (parameters.containsKey(name))
        {
            String value = (String) parameters.get(name);
            positions.put(name, value);
            return value;
        }
        else
        {
            return positions.getOrDefault(name, defaultValue);
        }
    }

    private Map<String, String> getUserQueryPositions(Query query)
    {
        Map<String, Map<String, String>> positions = (Map<String, Map<String, String>>) session.get().get(QUERY_POSITIONS);
        if (positions == null)
        {
            positions = new HashMap<>();
            session.get().set(QUERY_POSITIONS, positions);
        }
        String queryKey = query.getEntity().getName() + "." + query.getName();
        return positions.computeIfAbsent(queryKey, k -> new HashMap<>());
    }

    private TableModel getFromTableBuilder(Query query, Map<String, Object> parameters)
    {
        TableBuilder tableBuilder;

        switch (query.getType())
        {
            case JAVA:
                try
                {
                    tableBuilder = (TableBuilder) Class.forName(query.getQuery()).newInstance();
                    break;
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    throw Be5Exception.internalInQuery(query, e);
                }
            case GROOVY:
                try
                {
                    Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                            query.getQuery(), query.getFileName());

                    if (aClass != null)
                    {
                        tableBuilder = (TableBuilder) aClass.newInstance();
                        break;
                    }
                    else
                    {
                        throw Be5Exception.internal("Class " + query.getQuery() + " is null.");
                    }
                }
                catch (NoClassDefFoundError | IllegalAccessException | InstantiationException e)
                {
                    throw new UnsupportedOperationException("Groovy feature has been excluded", e);
                }
            default:
                throw Be5Exception.internal("Not support operation type: " + query.getType());
        }

        if (tableBuilder == null)
        {
            throw Be5Exception.internal("TableBuilder " + query.getQuery() + " is null.");
        }

        injector.injectMembers(tableBuilder);
        tableBuilder.initialize(query, parameters);

        return tableBuilder.getTableModel();
    }
}
