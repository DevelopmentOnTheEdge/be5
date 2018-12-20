package com.developmentontheedge.be5.query.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.DpsTableBuilder;
import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.query.impl.CellFormatter;
import com.developmentontheedge.be5.query.impl.SqlTableBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.QueryExecutor;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.query.TableConstants.LIMIT;
import static com.developmentontheedge.be5.query.TableConstants.OFFSET;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.query.TableConstants.ORDER_DIR;


public class TableModelServiceImpl implements TableModelService
{
    private final Meta meta;
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final QueryExecutor queryService;
    private final UserInfoProvider userInfoProvider;
    private final CellFormatter cellFormatter;
    private final QuerySession querySession;

    @Inject
    public TableModelServiceImpl(Meta meta, UserAwareMeta userAwareMeta, CoreUtils coreUtils, GroovyRegister groovyRegister,
                                 Injector injector, QueryExecutor queryService, UserInfoProvider userInfoProvider,
                                 CellFormatter cellFormatter, QuerySession querySession)
    {
        this.meta = meta;
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;
        this.queryService = queryService;
        this.userInfoProvider = userInfoProvider;
        this.cellFormatter = cellFormatter;
        this.querySession = querySession;
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
        return new SqlTableBuilder(query, (Map<String, Object>) parameters, userInfoProvider.get(), queryService,
                userAwareMeta, meta, cellFormatter, coreUtils, querySession);
    }

    private TableModel getSqlTableModel(Query query, Map<String, Object> parameters)
    {
        int orderColumn = Integer.parseInt((String) parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = (String) parameters.getOrDefault(ORDER_DIR, "asc");
        int offset = Integer.parseInt((String) parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == Integer.MAX_VALUE)
        {
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        return new SqlTableBuilder(query, parameters, userInfoProvider.get(), queryService,
                userAwareMeta, meta, cellFormatter, coreUtils, querySession)
                .sortOrder(orderColumn, orderDir)
                .offset(offset)
                .limit(Math.min(limit, maxLimit))
                .build();
    }

    private TableModel getFromTableBuilder(Query query, Map<String, Object> parameters)
    {
        DpsTableBuilder tableBuilder;

        switch (query.getType())
        {
            case JAVA:
                try
                {
                    tableBuilder = (DpsTableBuilder) Class.forName(query.getQuery()).newInstance();
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
                        tableBuilder = (DpsTableBuilder) aClass.newInstance();
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

        injector.injectMembers(tableBuilder);
        tableBuilder.initialize(query, parameters);

        return tableBuilder.getTableModel();
    }
}
