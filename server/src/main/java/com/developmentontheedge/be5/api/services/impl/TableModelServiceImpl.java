package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.QueryService;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.query.impl.SqlTableBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.util.LayoutUtils;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.developmentontheedge.be5.api.RestApiConstants.LIMIT;
import static com.developmentontheedge.be5.api.RestApiConstants.OFFSET;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_DIR;


public class TableModelServiceImpl implements TableModelService
{
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final QueryService queryService;

    @Inject
    public TableModelServiceImpl(UserAwareMeta userAwareMeta, CoreUtils coreUtils, GroovyRegister groovyRegister,
                                 Injector injector, QueryService queryService)
    {
        this.userAwareMeta = userAwareMeta;
        this.coreUtils = coreUtils;
        this.groovyRegister = groovyRegister;
        this.injector = injector;
        this.queryService = queryService;
    }

    @Override
    public TableModel getTableModel(Query query, Map<String, ?> parameters)
    {
        switch (query.getType())
        {
            case D1:
            case D1_UNKNOWN:
                return getSqlTableModel(query, (Map<String, Object>) parameters);
            case GROOVY:
                return getGroovyTableModel(query, (Map<String, Object>) parameters);
            default:
                throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }
    }

    @Override
    public SqlTableBuilder builder(Query query, Map<String, ?> parameters)
    {
        return new SqlTableBuilder(query, (Map<String, Object>) parameters, UserInfoHolder.getUserInfo(), queryService, userAwareMeta);
    }

    private TableModel getSqlTableModel(Query query, Map<String, Object> parameters)
    {
        int orderColumn = Integer.parseInt((String) parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = (String) parameters.getOrDefault(ORDER_DIR, "asc");
        int offset      = Integer.parseInt((String) parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt((String) parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        parameters.remove(ORDER_COLUMN);
        parameters.remove(ORDER_DIR);
        parameters.remove(OFFSET);
        parameters.remove(LIMIT);

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if(limit == Integer.MAX_VALUE)
        {
            //todo move defaultPageLimit, to getQuerySettings(query)
            limit = Integer.parseInt(LayoutUtils.getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        return new SqlTableBuilder(query, parameters, UserInfoHolder.getUserInfo(), queryService, userAwareMeta)
                .sortOrder(orderColumn, orderDir)
                .offset(offset)
                .limit(Math.min(limit, maxLimit))
                .build();
    }

    private TableModel getGroovyTableModel(Query query, Map<String, Object> parameters)
    {
        try
        {
            Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                    query.getQuery(), query.getFileName());

            if(aClass != null)
            {
                TableBuilder tableBuilder = (TableBuilder) aClass.newInstance();

                tableBuilder.initialize(query, parameters);
                injector.injectMembers(tableBuilder);

                return tableBuilder.getTableModel();
            }
            else
            {
                throw Be5Exception.internal("Class " + query.getQuery() + " is null." );
            }
        }
        catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
        {
            throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
        }
    }
}
